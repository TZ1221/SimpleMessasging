package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.server.Prattle;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class PrattleTest {


    /**
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void mainTest() throws IOException, InterruptedException {
        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.setErr(new PrintStream(new FileOutputStream("server_err.txt")));
                    System.setOut(new PrintStream(new FileOutputStream("server_out.txt")));
                    Prattle.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.start();
        Thread.sleep(500);
        Socket client = new Socket("localhost", 4545);
        OutputStream outToServer = client.getOutputStream();
        PrintStream out = new PrintStream(outToServer);

        out.print("HLO 5 mandy 2 --");
        out.flush();
        Thread.sleep(200);

        out.print("BCT 5 mandy 15 hellofromclient");
        out.flush();
        Thread.sleep(300);

        out.print("BYE 5 Mandy 2 --");
        out.flush();
        Thread.sleep(200);

        server.interrupt();

        File errFile = new File("server_err.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(errFile)));
        String line ;

        //account for save error messages
        line = reader.readLine();
        line = reader.readLine();
        line = reader.readLine();
        line = reader.readLine();
        line = reader.readLine();
        line = reader.readLine();

        line = reader.readLine();
        assertEquals("HLO 5 mandy 2 --", line);
        line = reader.readLine();
        assertEquals("BCT 5 mandy 15 hellofromclient", line);
        reader.close();
        errFile.delete();

/*        File outFile = new File("server_out.txt");
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)));


        line = reader.readLine();
        assertEquals("\tBCT 5 mandy 15 hellofromclient", line);
        reader.close();
        outFile.delete();*/
    }
    @Test
    void removeClientTest() throws IOException, ClassNotFoundException {

        //Start up prattle to initialize the group lists and user lists
        Prattle.startUp();

        //Create a client to test things with
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        //Set the name of the client to whatever you want it to be
        client.setName("testBig");
        //Make the client be able to receive messages
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client.setDebug(true);
        //This is where you would add other things, depending on what you are doing you will need fields in clientRunnable
        //to be initialized to avoid null errors

        //Set prattle debug to true to be able to return testUser and testGroup
        Prattle.setDebug(true);
        //Set testUser
        Prattle.setTestUser(new UserStorage("testBig", "password"));

        //Functionality of test

        //add client to prattle
        Prattle.addClient(client);
        //prove that it was added successfully
        assertTrue(Prattle.connectedClients().contains(client));
        //remove client
        Prattle.removeClient(client);
        //prove it
        assertFalse(Prattle.connectedClients().contains(client));

        Prattle.removeClient(client);

    }

    @Test
    void sendMessageTest() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        client.setName("sendMessageTest");
        //Make the client be able to receive messages
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client.setDebug(true);
        Prattle.addClient(client);
        ClientRunnable otherClient = new ClientRunnable(SocketChannel.open());
        otherClient.setName("otherSendMessageTest");
        //Make the client be able to receive messages
        otherClient.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        otherClient.setDebug(true);
        Prattle.addClient(otherClient);

        Prattle.register("sendMessageTest", "password");
        Prattle.register("otherSendMessageTest", "password");

        PrintStream oldErr = System.err;
        System.setErr(new PrintStream(new FileOutputStream("sendMessageTest.txt")));

        File errFile = new File("sendMessageTest.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(errFile)));
        String line ;

        Message testsMessage = Message.makeMessage(Message.MessageType.PRIVATE.toString(), "sendMessageTest", "Hello");
        Prattle.sendMessage("sendMessageTest", testsMessage);


        //remove false
        line = reader.readLine();
        line = reader.readLine();
        assertEquals("Message Sent to: sendMessageTest from: sendMessageTestHello", line);

        Prattle.shutDown();

        System.setErr(oldErr);
    }


    @Test
    void saveLoadTest() throws IOException, ClassNotFoundException {
        UserStorage testUser = new UserStorage("Test", "1");
        String savePath = Prattle.save(testUser, "testUser");
        UserStorage revivedUser = (UserStorage) Prattle.load(savePath);
        assertEquals(testUser.getName(), revivedUser.getName());
    }

    @Test
    void loginTest() throws IOException, ClassNotFoundException {

        Prattle.startUp();
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        client.setName("loginTest");
        //Make the client be able to receive messages
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client.setDebug(true);
        Prattle.addClient(client);


        Prattle.register("loginTest", "password");
        Prattle.login("loginTest", "password");

        Prattle.createGroup("newGroup");
        Prattle.joinGroup("loginTest", "newGroup");

        //logged out user
        Prattle.removeClient(client);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        Prattle.sendMessage("loginTest", Message.makeHelloMessage("hello"));
        Prattle.sendGroupMessage("newGroup", "differentuser", "yayayay");
        Prattle.shutDown();

        Prattle.addClient(client);

        Prattle.login("loginTest", "password");











        /*
        Prattle.startUp();
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        client.setName("loginTest");
        client.waitingList = new ConcurrentLinkedQueue<>();
        client.debug = true;
        Prattle.addClient(client);

        Prattle.createGroup("loginGroup");

        //Set prattle debug to true to be able to return testUser and testGroup
        Prattle.debug = true;

        Prattle.register("loginTest", "password");

        //Set testUser
        UserStorage user = new UserStorage("loginTest", "password");
        user.addOfflineMessage(Message.makeHelloMessage("hello"));
        user.addGroup("loginGroup");
        user.logout();
        Prattle.shutDown();


        Prattle.testUser = user;






        Prattle.joinGroup("loginTest", "loginGroup");
        Prattle.sendGroupMessage("loginGroup", "loginTest", "Hello");
        Prattle.sendMessage("loginTest", Message.makeHelloMessage("hola"));

        Prattle.sendMessage("kdhagkhg", Message.makeHelloMessage("hallo"));


        Prattle.login("loginTest", "password");

        Prattle.deleteUser("loginTest");
        Prattle.deleteGroup("loginGroup");


        Prattle.removeClient(client);

        Prattle.shutDown();
*/
    }

    @Test
    void sendGroupMessage() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        Prattle.createGroup("testsendGroupMessage");
        Prattle.sendGroupMessage("testsendGroupMessage", "Test", "test message");


    }



    @Test
    void shutDown() throws IOException {
        PrintStream old = System.err;
        try {
            System.setErr(new PrintStream(new FileOutputStream("shutdown.txt")));
            Prattle.shutDown();

            File shutDownFile = new File("shutdown.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(shutDownFile)));
            String line;

            //remove save msg
            line = reader.readLine();
            line = reader.readLine();

            line = reader.readLine();
            assertEquals("GroupList saved to groupList.ser", line);
            line = reader.readLine();
            assertEquals("UserList saved to userList.ser", line);
            assertFalse(Prattle.isInitialStartUp());
        } finally {
            System.setErr(old);
        }
    }

    @Test
    void startUp() throws IOException, ClassNotFoundException {
        PrintStream old = System.err;
        try {
            System.setErr(new PrintStream(new FileOutputStream("startup.txt")));
            Prattle.startUp();

            File startUpFile = new File("startup.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(startUpFile)));
            String line;

            line = reader.readLine();
            assertEquals("GroupList loaded", line);
            line = reader.readLine();
            assertEquals("UserList loaded", line);



            assertFalse(Prattle.isInitialStartUp());
        } finally {
            System.setErr(old);
        }
    }


    @Test
    void joinGroup() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        if(Prattle.createGroup("Testgroup")) {
            //group was created successfully
            assertTrue(Prattle.joinGroup("GroupJoiner", "Testgroup"));
        }
    }

    @Test
    void deleteGroup() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        if(Prattle.createGroup("testDeleteGroup")) {
            assertTrue(Prattle.deleteGroup("testDeleteGroup"));
        }
    }

    @Test
    void leaveGroupTest() throws IOException, ClassNotFoundException {


        Prattle.startUp();



        //Create a client to test things with
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        //Set the name of the client to whatever you want it to be
        client.setName("leaveGroupUser");
        //Make the client be able to receive messages
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client.setDebug(true);
        //This is where you would add other things, depending on what you are doing you will need fields in clientRunnable
        //to be initialized to avoid null errors

        Prattle.addClient(client);

        //Create a client to test things with
        ClientRunnable client2 = new ClientRunnable(SocketChannel.open());
        //Set the name of the client to whatever you want it to be
        client2.setName("stayLoggedInUser");
        //Make the client be able to receive messages
        //Make the client be able to receive messages
        client2.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client2.setDebug(true);
        //This is where you would add other things, depending on what you are doing you will need fields in clientRunnable
        //to be initialized to avoid null errors

        Prattle.addClient(client2);


        Prattle.createGroup("leaveGroupTest");
        //group created, or already exists

        try {
            assertTrue(Prattle.joinGroup("leaveGroupUser", "leaveGroupTest"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        assertTrue(Prattle.leaveGroup("leaveGroupUser", "leaveGroupTest"));

        Prattle.deleteGroup("leaveGroupTest");


        //Test group staying logged in

        Prattle.createGroup("userStillLoggedIn");

        //register users to add them to active users
        Prattle.register("leaveGroupUser", "password");
        Prattle.register("stayLoggedInUser", "password");

        Prattle.login("leaveGroupUser", "password");
        Prattle.login("stayLoggedInUser", "password");


        assertTrue(Prattle.joinGroup("leaveGroupUser", "userStillLoggedIn"));
        assertTrue(Prattle.joinGroup("stayLoggedInUser", "userStillLoggedIn"));
        //orginal user logs off
        assertTrue(Prattle.leaveGroup("leaveGroupUser", "userStillLoggedIn"));

        Prattle.addActiveGroup(new Group("userStillLoggedIn"));
        assertNotNull(Prattle.returnGroup("userStillLoggedIn"));

        //Remove users so they dont through errors in repeat tests
        Prattle.deleteUser("leaveGroupUser");
        Prattle.deleteUser("stayLoggedInUser");

        //group delete
        Prattle.deleteGroup("userStillLoggedIn");
        Prattle.shutDown();

    }

    /**
     * FINISHED
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void createGroupTest() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        assertTrue(Prattle.createGroup("createGroupTest"));
        //cannot create same group
        assertFalse(Prattle.createGroup("createGroupTest"));

        Prattle.deleteGroup("createGroupTest");

    }


    @Test
    public void testBig() throws IOException, ClassNotFoundException {
        Prattle.startUp();
        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        client.setName("testBig");
        //Make the client be able to receive messages
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        client.setDebug(true);
        Prattle.addClient(client);
        ClientRunnable otherClient = new ClientRunnable(SocketChannel.open());
        otherClient.setName("testOtherBig");
        //Make the client be able to receive messages
        otherClient.setWaitingList(new ConcurrentLinkedQueue<>());
        //Set debug to true, to avoid null errors
        otherClient.setDebug(true);
        Prattle.addClient(otherClient);

        Prattle.register("testBig", "password");
        Prattle.register("testOtherBig", "password");


        Message testsMessage = Message.makeMessage(Message.MessageType.PRIVATE.toString(), "testBig", "Hello");
        Prattle.sendMessage("testBig", testsMessage);

        Prattle.shutDown();






    }

}