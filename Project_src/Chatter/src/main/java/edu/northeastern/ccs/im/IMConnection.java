package edu.northeastern.ccs.im;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.SwingWorker;

/**
 * This class manages the connection between an the IM client and the IM server.
 * Instances of this class can be relied upon to manage all the details of this
 * connection and sends alerts when appropriate. Instances of this class must be
 * constructed and connected before it can be used to transmit messages.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class IMConnection {

	/**
	 * Real Connection instance which this class wraps and makes presentable to the
	 * user
	 */
	private ChatterSocketNB socketConnection;

	/**
	 * List of instances that have registered as a listener for connection events.
	 */
	private Vector<LinkListener> linkListeners;

	/**
	 * List of instances that have registered as a listener for received message
	 * events.
	 */
	private Vector<MessageListener> messageListeners;

	/** Server to which this connection will be made. */
	private String hostName;

	/** Port to which this connection will be made. */
	private int portNum;

	/** Name of the user for which this connection was formed. */
	private String userName;

	/** Name of the user for which this connection was formed combine with password. */
	private String userNamePlusPassword;

	/**
	 * Holds the SwingWorker which is used to read and process all incoming data.
	 */
	private SwingWorker<Void, ChatterMessage> workerBee;

	/** Instance used to read the messages. */
	private MessageScanner messageScanner;

	/**
	 * Creates an instance that will manage a connection with an IM server, but does
	 * not begin the process of making a connection to the IM server.
	 *
	 * @param host     The name of the host that this connection is using
	 * @param port     The port number to use.
	 */
	public IMConnection(String host, int port) {
		linkListeners = new Vector<LinkListener>();
		messageListeners = new Vector<MessageListener>();
		hostName = host;
		portNum = port;
	}

	/**
	 * Add the given listener to be notified whenever 1 or more Messages are
	 * received from IM server via this connection.
	 *
	 * @param listener Instance which will begin to receive notifications of any
	 *                 messages received by this IMConnection.
	 * @throws InvalidListenerException Exception thrown when this is called with a
	 *                                  value of null for {@code listener}
	 */
	public void addMessageListener(MessageListener listener) {
		if (listener == null) {
			throw new InvalidListenerException("Cannot add (null) as a listener!");
		}
		messageListeners.add(listener);
	}

	/**
	 * Send a message to log in to the IM server using the given username. For the
	 * moment, you will automatically be logged in to the server, even if there is
	 * already someone with that username.<br/>
	 * Precondition: connectionActive() == false
	 *
	 * @throws IllegalNameException Exception thrown if we try to connect with an
	 *                              illegal username. Legal usernames can only
	 *                              contain letters and numbers.
	 * @return True if the connection was successfully made; false otherwise.
	 */
	public boolean connect() {

		try {
			socketConnection = new ChatterSocketNB(hostName, portNum);
			socketConnection.startIMConnection();
		} catch (IOException e) {
			// Report the error
			System.err.println("ERROR:  Could not make a connection to: " + hostName + " at port " + portNum);
			System.err.println(
					"        If the settings look correct and your machine is connected to the Internet, report this error to Dr. Jump");
			// And print out the problem
			e.printStackTrace();
			// Return that the connection could not be made.
			return false;
		}

		MessageScanner rms = MessageScanner.getInstance();
		addMessageListener(rms);
		messageScanner = rms;


		return true;
	}

	/**
	 * Returns whether the instance is managing an active, logged-in connection
	 * between the client and an IM server.
	 *
	 * @return True if the client is logged in to the server using this connection;
	 *         false otherwise.
	 */
	public boolean connectionActive() {
		if (socketConnection == null) {
			return false;
		} else {
			return socketConnection.isConnected();
		}
	}

	/**
	 * Break this connection with the IM server. Once this method is called, this
	 * instance will need to be logged back in to the IM server to be usable.<br/>
	 * Precondition: connectionActive() == true
	 */
	public void disconnect() {
		ChatterMessage quitChatterMessage = ChatterMessage.makeQuitMessage(getUserName());
		socketConnection.print(quitChatterMessage);
		KeyboardScanner.close();
	}

	/**
	 * Gets an object which can be used to read what the user types in on the
	 * keyboard without waiting. The object returned by this method should be used
	 * rather than {@link Scanner} since {@code Scanner} will cause a program to
	 * halt if there is no input.
	 *
	 * @return Instance of {@link KeyboardScanner} that can be used to read keyboard
	 *         input for this connection of the server.
	 */
	public KeyboardScanner getKeyboardScanner() {
		return KeyboardScanner.getInstance();
	}

	/**
	 * Gets an object which can be used to get the message sent by the server over
	 * this connection. This is the only object that can be used to retrieve all
	 * these messages.
	 *
	 * @return Instance of {@link MessageScanner} that can be used to read message
	 *         sent over this connection for this user.
	 */
	public MessageScanner getMessageScanner() {
		if (messageScanner == null) {
			throw new IllegalOperationException("Cannot get a MessageScanner if you have not connected to the server!");
		}
		return messageScanner;
	}

	/**
	 * Get the name of the user for which we have created this connection.
	 *
	 * @return Current value of the user name and/or the username with which we
	 *         logged in to this IM server.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Unless this is a &quot;special&quot; server message, this sends the given
	 * message to all of the users logged in to the IM server. <br/>
	 * Precondition: connectionActive() == true
	 *
	 * @param message Text of the message which will be broadcast to all users.
	 */
	public boolean sendMessage(String message) {
		if (!connectionActive()) {
			throw new IllegalOperationException("Cannot send a message if you are not connected to a server!\n");
		}
		String[] messages = message.split(" ", 2);
		if (messages.length != 2) return false;
		String action = messages[0];
		String text = messages[1];
		ChatterMessage chatterMessage = null;
		if (action.equals("create")) {
			chatterMessage = ChatterMessage.makeCreateMessage(userName, text);
		} else if (action.equals("join")) {
			chatterMessage = ChatterMessage.makeJoinMessage(userName, text);
		} else if (action.equals("group")) {
			chatterMessage = ChatterMessage.makeGroupMessage(userName, text);
		} else if (action.equals("private")) {
			chatterMessage = ChatterMessage.makePrivateMessage(userName, text);
		} else if (action.equals("delete")) {
			chatterMessage = ChatterMessage.makeDeleteMessage(userName, text);
		} else if (action.equals("leave")) {
			chatterMessage = ChatterMessage.makeLeaveMessage(userName, text);
		}
		if (chatterMessage == null) {
			return false;
		}
		socketConnection.print(chatterMessage);
		return true;
	}

	/**
	 * Unless this is a &quot;special&quot; server message, this sends the given
	 * message to all of the users logged in to the IM server. <br/>
	 * Precondition: connectionActive() == true
	 *
	 * @param username Text of the message which will be broadcast to all users.
	 * @param password Text of the message which will be broadcast to all users.
	 * @param action Text of the message which will be broadcast to all users.
	 */
	public void sendUserInfoMessage(String username, String password, String action) {
		this.userName = username;
		if (!connectionActive()) {
			throw new IllegalOperationException("Cannot send a message if you are not connected to a server!\n");
		}
		ChatterMessage message;
		MessageDigest md;
		String encryptedpassword = "";
		try {
			md = MessageDigest.getInstance("MD5");

			md.update(password.getBytes());

			encryptedpassword = new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ("Login".equals(action)) {
			message = ChatterMessage.makeLoginMessage(username, encryptedpassword);
		} else {
			message = ChatterMessage.makeRegisterMessage(username, encryptedpassword);
		}
		socketConnection.print(ChatterMessage.makeHelloMessage("initialize"));
		try {
			Thread.sleep(1000);
		} catch (Exception e) {

		}
		socketConnection.print(message);
		// Create the background thread that handles our incoming messages.
		workerBee = new ScanForMessagesWorker(this, socketConnection);
		// Start the worker bee scanning for messages.
		workerBee.execute();
	}

	@SuppressWarnings({ "unchecked" })
	protected void fireSendMessages(List<ChatterMessage> mess) {
		Vector<MessageListener> targets;
		synchronized (this) {
			targets = (Vector<MessageListener>) messageListeners.clone();
		}
		for (MessageListener iml : targets) {
			iml.messagesReceived(mess.iterator());
		}
	}

	@SuppressWarnings("unchecked")
	protected void fireStatusChange(String userName) {
		Vector<LinkListener> targets;
		synchronized (this) {
			targets = (Vector<LinkListener>) linkListeners.clone();
		}
		for (LinkListener iml : targets) {
			iml.linkStatusUpdate(userName, this);
		}
	}

	protected void loggedOut() {
		socketConnection = null;
	}
}