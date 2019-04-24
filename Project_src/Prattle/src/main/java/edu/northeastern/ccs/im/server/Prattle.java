package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Group;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.UserStorage;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. 
 * It does not send a response when the user has gone off-line.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public abstract class Prattle {

    private final static Logger LOGGER = Logger.getLogger(Prattle.class.getName());
	/** Amount of time we should wait for a signal to arrive. */
	private static final int DELAY_IN_MS = 50;


	/** Number of threads available in our thread pool. */
	private static final int THREAD_POOL_SIZE = 20;

	/** Delay between times the thread pool runs the client check. */
	private static final int CLIENT_CHECK_DELAY = 200;

    /**
     * Delay between saves of server
     */
	private static final int AUTO_SAVE_DELAY = 10000;

	/** Collection of threads that are currently being used. */
	private static ConcurrentLinkedQueue<ClientRunnable> active;

	/** All of the static initialization occurs in this "method" */
	static {
		// Create the new queue of active threads.
		active = new ConcurrentLinkedQueue<>();
	}

    /** Collection of threads that are currently being used. */
    private static List<UserStorage> activeUsers;

    /** All of the static initialization occurs in this "method" */
    static {
        // Create the new queue of active threads.
        activeUsers = Collections.synchronizedList(new LinkedList<UserStorage>());
    }
    /** Collection of threads that are currently being used. */
    private static List<Group> activeGroups;

    /** All of the static initialization occurs in this "method" */
    static {
        // Create the new queue of active threads.
        activeGroups = Collections.synchronizedList(new LinkedList<Group>());
    }


    /**
	 * Broadcast a given message to all the other IM clients currently on the
	 * system. This message _will_ be sent to the client who originally sent it.
	 * 
	 * @param message Message that the client sent.
	 */
	public static void broadcastMessage(Message message) {
		// Loop through all of our active threads
		for (ClientRunnable tt : active) {
			// Do not send the message to any clients that are not ready to receive it.
			if (tt.isInitialized()) {
				tt.enqueueMessage(message);
			}
		}
	}

	/**
	 * Start up the threaded talk server. This class accepts incoming connections on
	 * a specific port specified on the command-line. Whenever it receives a new
	 * connection, it will spawn a thread to perform all of the I/O with that
	 * client. This class relies on the server not receiving too many requests -- it
	 * does not include any code to limit the number of extant threads.
	 * 
	 * @param args String arguments to the server from the command line. At present
	 *             the only legal (and required) argument is the port on which this
	 *             server should list.
	 * @throws IOException Exception thrown if the server cannot connect to the port
	 *                     to which it is supposed to listen.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException {

        Prattle.startUp();

        // Connect to the socket on the appropriate port to which this server connects.
		try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
			// Create the Selector with which our channel is registered.
			Selector selector = SelectorProvider.provider().openSelector();
			// Register to receive any incoming connection messages.
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			// Create our pool of threads on which we will execute.
			ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
			// Listen on this port until ...
            @SuppressWarnings("rawtypes")
            ScheduledFuture saveFuture = threadPool.scheduleAtFixedRate(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    Prattle.shutDown();
                                                                                } catch (IOException e) {
                                                                                    LOGGER.log(Level.SEVERE, e.toString());
                                                                                }
                                                                            }
                                                                        }, AUTO_SAVE_DELAY,
                    AUTO_SAVE_DELAY, TimeUnit.MILLISECONDS);

			boolean done = false;
			while (!done) {
                // Check if we have a valid incoming request, but limit the time we may wait.
				while (selector.select(DELAY_IN_MS) != 0) {
					// Get the list of keys that have arrived since our last check
					Set<SelectionKey> acceptKeys = selector.selectedKeys();
					// Now iterate through all of the keys
					Iterator<SelectionKey> it = acceptKeys.iterator();
					while (it.hasNext()) {
						// Get the next key; it had better be from a new incoming connection
						SelectionKey key = it.next();
						it.remove();
						// Assert certain things I really hope is true
						assert key.isAcceptable();
						assert key.channel() == serverSocket;
						// Create a new thread to handle the client for which we just received a
						// request.
						try {
							// Accept the connection and create a new thread to handle this client.
							SocketChannel socket = serverSocket.accept();
							// Make sure we have a connection to work with.
							if (socket != null) {
								ClientRunnable tt = new ClientRunnable(socket);
								// Add the thread to the queue of active threads
								active.add(tt);
								// Have the client executed by our pool of threads.
								@SuppressWarnings("rawtypes")
								ScheduledFuture clientFuture = threadPool.scheduleAtFixedRate(tt, CLIENT_CHECK_DELAY,
										CLIENT_CHECK_DELAY, TimeUnit.MILLISECONDS);
								tt.setFuture(clientFuture);
							}
						} catch (AssertionError ae) {
							System.err.println("Caught Assertion: " + ae.toString());
						} catch (Exception e) {
							System.err.println("Caught Exception: " + e.toString());
						}
					}
				}
			}
		}

	}
    /**
	 * Remove the given IM client from the list of active threads.
	 * 
	 * @param dead Thread which had been handling all the I/O for a client who has
	 *             since quit.
	 */
	public static void removeClient(ClientRunnable dead) {
		// Test and see if the thread was in our list of active clients so that we
		// can remove it.
        String userName = dead.getName();
        //we need to remove it from the active users
        UserStorage logOutUser = returnClient(userName);
        activeUsers.remove(logOutUser);

        assert(logOutUser != null);
        logOutUser.logout();

        try {
            Prattle.save(logOutUser, "Removing client");
        } catch (IOException e) {
           LOGGER.log(Level.SEVERE, e.toString());
        }

        if (!active.remove(dead)) {
			System.out.println("Could not find a thread that I tried to remove!\n");
		}
	}


/*
	After this is my stuff, tried to keep it seperate in case we want to rollback at some point
	 */

    /**
     * Sends a message to the username specified
     * @param userName
     * @param message
     */
    public static boolean sendMessage(String userName, Message message) {
        boolean sent = false;
        boolean online = false;

        if(userList.containsKey(userName)) {
            try {
                //username is registered
                UserStorage loggedOff = null;
                try {
                    loggedOff = (UserStorage) Prattle.load(userList.get(userName));
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.toString());
                }
                assert(loggedOff != null);
                for(ClientRunnable client : active) {
                    if (client.getName().equals(userName)) {

                        message.setIPaddress( client.getIPaddress());

                        client.enqueueMessage(message);
                        online = true;
                    }
                }
                if(online) {
                    //record the message in user histrory
                    loggedOff.addMessage(message);
                    sent = true;
                } else {
                    //give it to the object to deal with
                    loggedOff.addOfflineMessage(message);
                    sent = true;
                }
                Prattle.save(loggedOff, "loggedOffUser");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.toString());
            }
        }

        if(sent) {
            //if true
            System.err.println("Message Sent to: " + userName + " from: " + message.getName() + message.getText());
            System.err.println(message);
        }
        return sent;

    }

    /**
     * Creates a group with the groupname
     * @param groupName
     * @return true if group was creates, false if group already exists
     */
    public static boolean createGroup(String groupName) {
        boolean result = false;
        if(groupList.containsKey(groupName)) {
            //group already exists do nothing
            System.err.println("Group already exists");
        } else {
            try {
                //group doesnt exist
                Group newGroup = new Group(groupName);
                String saveLocation = Prattle.save(newGroup, "newGroup");
                groupList.put(groupName, saveLocation.toString());
                result = true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.toString());
            }

        }
        return result;
    }

    /**
     *
     * @param userName
     * @param password
     * @return true if client is able to register
     */
    public static boolean register(String userName, String password) {
        boolean result = false;
        if(userList.containsKey(userName)) {
            //user already exists
        } else {
            try {
                UserStorage newUser = new UserStorage(userName, password);
                String saveLocation = Prattle.save(newUser, "registerFunction");
                userList.put(userName, saveLocation.toString());
                result = true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.toString());
            }
        }
        return result;
    }


    /**
     * Returns a path respresnting the save location of the object passed
     * @param objectToSave
     * @return
     */
    public static String save(Object objectToSave, String saveTitle) throws IOException {
            String returnPath = null;
            if(objectToSave instanceof  UserStorage) {
                // write object to file
                //save to Server/Users
                //c:\\temp\\address.ser
                File f = new File("Users/" +((UserStorage) objectToSave).getName() + ".ser");
                if (f.getParentFile() != null) {
                    f.getParentFile().mkdirs();
                }
                System.err.println("f.createFile() line 324 (User) Prattle.Save(): " + f.createNewFile());


                //String stringRepresentation = "/User/" + ((UserStorage) objectToSave).getName() + ".ser";
                FileOutputStream fileOutputStream = new FileOutputStream(f.getAbsoluteFile());
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                    objectOutputStream.writeObject(objectToSave);
                }
                returnPath = f.getPath();

            } else if ( objectToSave instanceof Group) {
                //Sever/Groups
                File f = new File("Group/" +((Group) objectToSave).returnGroupName() + ".ser");
                if (f.getParentFile() != null) {
                    f.getParentFile().mkdirs();
                }

                System.err.println("f.createFile() line 341 (Group) Prattle.Save(): " + f.createNewFile());

                //String stringRepresentation = "/User/" + ((UserStorage) objectToSave).getName() + ".ser";
                FileOutputStream fileOutputStream = new FileOutputStream(f.getAbsoluteFile());
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                    objectOutputStream.writeObject(objectToSave);
                }
                returnPath = f.getPath();

            } else if(objectToSave instanceof ConcurrentHashMap) {
                //Sever/Groups
                File f = new File(saveTitle + ".ser");
                if (f.getParentFile() != null) {
                    f.getParentFile().mkdirs();
                }

                System.err.println("f.createFile() line 357(HashMap) Prattle.Save(): " + f.createNewFile());

                //String stringRepresentation = "/User/" + ((UserStorage) objectToSave).getName() + ".ser";
                FileOutputStream fileOutputStream = new FileOutputStream(f.getAbsoluteFile());
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                    objectOutputStream.writeObject(objectToSave);
                }
                returnPath = f.getPath();


            } else {
                //Sever/Groups
//                File f = new File(objectToSave.toString() + ".ser");
//                if (f.getParentFile() != null) {
//                    f.getParentFile().mkdirs();
//                }
//                f.createNewFile();
//
//                //String stringRepresentation = "/User/" + ((UserStorage) objectToSave).getName() + ".ser";
//                FileOutputStream fileOutputStream = new FileOutputStream(f.getAbsoluteFile());
//                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
//                    objectOutputStream.writeObject(objectToSave);
//                }
//                returnPath = f.getPath();
                returnPath = "objectnotsaved";

            }
            return returnPath;
    }

    //represents a list of the usernames and a string representing the location of hte user object on file
    private static ConcurrentHashMap<String, String> userList = new ConcurrentHashMap<>(50);
    private static ConcurrentHashMap<String, String> groupList = new ConcurrentHashMap<>(50);

    /**
     * Returns the client youre looking for *jedi hand wave*
     * @param username
     * @return
     */
    private static ClientRunnable getClient(String username) {
        for(ClientRunnable client : active) {
            if(client.getName().equals(username)) {
                return client;
            }
        }
        return null;
    }

    /**
     * Logs the user in
     * @param userName
     * @param password
     */
    public static boolean login(String userName, String password)  {
        boolean result = false;
        ClientRunnable client = getClient(userName);
        assert(client != null);
        //first validate username
        if(!userList.containsKey(userName)) {
            //name is not on list send NAK message
        } else {
            //user already exists
            UserStorage revivedUser = null;
            try {
                revivedUser = (UserStorage) Prattle.load(userList.get(userName));
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, e.toString());
            }
            assert(revivedUser != null);
            //validate login information.
            if(password.equals(revivedUser.getPassword())) {
                //the password works log the user in
                result = true;
                //then send it all the messages it needs
                Queue<Message> unsentMessages = revivedUser.getMessages();
                //send client those messages
                for(Message msg : unsentMessages) {
                    client.enqueueMessage(msg);
                    //so we also need to add teh messages to the list of messages the client has
                    revivedUser.addMessage(msg);
                }
                activeUsers.add(revivedUser);
                //also should grab messages from group
                for(String groupName : revivedUser.getGroups()) {
                    //group exisits
                    Group userGroup = returnGroup(groupName);
                    //if its null its not in the active list
                    if(userGroup == null) {

                    try {
                        userGroup = (Group) Prattle.load(groupList.get(groupName));
                    } catch (IOException | ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, e.toString());
                    } }
                    assert (userGroup != null);
                    // grab groups from server then send them this,
                    Date lastLogin = revivedUser.getLastLogin();
                    List<Message> unsentGroupMessages = userGroup.messagesSinceTime(lastLogin);
                    for(Message gmsg : unsentGroupMessages) {
                        //print to client
                        client.enqueueMessage(gmsg);
                        //record to user
                        revivedUser.addMessage(gmsg);
                    }
                    activeGroups.add(userGroup);

                }


            }
        }



        return result;
    }

    /**
     * Sends a message to the group from the username
     * @param groupName
     * @param userName
     * @param text
     * @return true if message sent, false if not(group was invalid)
     */
    public static boolean sendGroupMessage(String groupName,  String userName, String text) {
        boolean result = false;
        Message msg = Message.makeBroadcastMessage(userName, text);
        //if its already a group
        if(groupList.containsKey(groupName)) {
            try {
                //first check active groups if its in the active groups send the message to that
                for(Group activeGroup : activeGroups) {
                    if(activeGroup.returnGroupName().equals(groupName)) {
                        try {
                            activeGroup.groupBroadcastMessage(msg);
                        } catch (Group.debugException e) {
                            LOGGER.log(Level.SEVERE, e.toString());
                        }
                        result = true;
                    }
                }
                if(!result) {
                    //group is not active, but exisits
                    Group group = (Group) Prattle.load(groupList.get(groupName));
                    try {
                        group.groupBroadcastMessage(msg);
                    } catch (Group.debugException e) {
                        LOGGER.log(Level.SEVERE, e.toString());
                    }
                    Prattle.save(group, "sent to unactive group");
                }
            } catch (IOException  | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, e.toString());
            }
            result = true;
        }

        return result;

    }

    /**
     * Loads an object
     * @param filePath
     * @return object from file
     */
    public static Object load(String filePath) throws IOException, ClassNotFoundException {
        String file = filePath.replace('\\', '/');
        // read object from file
            File newFile = new File(file);
            FileInputStream fis = new FileInputStream(newFile);
        Object result;
        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            result = ois.readObject();
        }
        return result;
    }


    public static void shutDown() throws IOException {
        //we need to save the grouplist and the userlist
         String userListPath = Prattle.save(userList, "userList");
         String groupListPath = Prattle.save(groupList, "groupList");
         System.err.println("GroupList saved to " + groupListPath);
        System.err.println("UserList saved to " + userListPath);
        initialStartUp = false;
    }

    private static boolean initialStartUp = true;

    /**
     * Runs at startup to create or load the users and group lists
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static void startUp() throws IOException, ClassNotFoundException {
        try {
            groupList =(ConcurrentHashMap<String, String>) load("groupList.ser");
            userList =(ConcurrentHashMap<String, String>) load("userList.ser");
            System.err.println("GroupList loaded");
            System.err.println("UserList loaded");
        } catch (FileNotFoundException e) {
            groupList = new ConcurrentHashMap<>();
            userList = new ConcurrentHashMap<>();
        }
        //saves grouplist and userlist
        Prattle.shutDown();
    }

    public static boolean joinGroup(String username, String groupName) {
        boolean result = false;
        if(groupList.containsKey(groupName)) {
            //group exisits
            Group joinGroup = returnGroup(groupName);
            //if its null its not in the active list
            if(joinGroup == null) {
                try {
                    joinGroup = (Group) Prattle.load(groupList.get(groupName));
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.toString());
                }
                assert (joinGroup != null);
                activeGroups.add(joinGroup);
            }
            joinGroup.addUser(username);
            UserStorage user = returnClient(username);
            if(user == null) {
                try{
                    user = (UserStorage) Prattle.load(userList.get(username));
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.toString());
                }
            }
            assert(user != null);
            user.addGroup(groupName);
            result = true;

        }
        //group doesnt exist, return false
        return result;
    }

    /**
     * deletes a group
     *
     * @param groupName the group to delete
     * @return true if group was deleted
     */
    public static boolean deleteGroup(String groupName) {
        Group groupToDelete = returnGroup(groupName);
        activeGroups.remove(groupToDelete);
     /*   groupList.remove(groupName);*/
        return groupList.remove(groupName) instanceof String;
    }

    /**
     * deletes a user
     *
     * @param userName the group to delete
     * @return true if user was deleted
     */
    public static boolean deleteUser(String userName) {
        UserStorage userToDelete = returnClient(userName);
        activeUsers.remove(userToDelete);
        userList.remove(userName);
        return userList.containsKey(userName);
    }

    /**
     * Takes the username out of the group
     * @param username
     * @param groupName
     * @return
     */
    public static boolean leaveGroup(String username, String groupName) {

        //group you are leaving should be logged in, and if anyone else is still logged in then it shouldet be logged out
        boolean result = false;

        if(groupList.containsKey(groupName)) {
            //group exisits and is on active list
            Group leavingGroup = returnGroup(groupName);
            if(leavingGroup == null) {
                try {
                    leavingGroup = (Group) Prattle.load(groupList.get(groupName));
                } catch (IOException |ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.toString());
                }
            }
            assert(leavingGroup != null);
            if(leavingGroup.hasUser(username)) {
                leavingGroup.removeUser(username);
                result = true;
                boolean allUsersLoggedOff = true;
                for(UserStorage active : activeUsers) {
                    if(leavingGroup.hasUser(active.getName())) {
                        allUsersLoggedOff = false;
                    }
                }
                if(allUsersLoggedOff) {
                    activeGroups.remove(leavingGroup);
                    try {
                        Prattle.save(leavingGroup, "no members online group logging off");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.toString());
                    }
                }
            }


        }
        //group doesnt exist, will return false
        return result;
    }

    /**
        *
         * Checks if the group is a group, if it is finds it, first check active g
         * @param groupName
         * @return
     * */

    public static Group returnGroup(String groupName) {
        for(Group group : activeGroups) {
            if(group.returnGroupName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    public static void addActiveGroup(Group groupToAdd) {
        activeGroups.add(groupToAdd);
    }


    /** checks if the user is a user, if it finds it,
     * then return the user
     *
     * @param userName the user to find
     * @return the UserStorage object associated with the username
     */
    public static UserStorage returnClient(String userName) {
        for(UserStorage user : activeUsers) {
            if(user.getName().equals(userName)) {
                return user;
            }
        }
        if(debug) {
            return testUser;
        } else {
            return null;
        }
    }

    private static boolean debug = false;

    public static boolean isInitialStartUp() {
        return initialStartUp;
    }

    public static void setInitialStartUp(boolean initialStartUp) {
        Prattle.initialStartUp = initialStartUp;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        Prattle.debug = debug;
    }

    public static UserStorage getTestUser() {
        return testUser;
    }

    public static void setTestUser(UserStorage testUser) {
        Prattle.testUser = testUser;
    }

    private static UserStorage testUser;


    public static void addClient(ClientRunnable client) {
        active.add(client);
    }

    public static ConcurrentLinkedQueue connectedClients() {
        return active;
    }


}


