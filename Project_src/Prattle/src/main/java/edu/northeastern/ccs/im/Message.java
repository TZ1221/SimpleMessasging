package edu.northeastern.ccs.im;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Date;

/**
 * Each instance of this class represents a single transmission by our IM
 * clients.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class Message implements Serializable {

	private static final long serialVersionUID = -7414657607704234998L;

	/**
	 * List of the different possible message types.
	 */
	protected enum MessageType {
	/**
	 * ChatterMessage sent by the user attempting to login using a specified username and password.
	 */
	LOGIN("LGN"),

	/**
	 * ChatterMessage sent by the user attempting to register using a specified username and password.
	 */
	REGISTER("RGT"),

    /**
     * ChatterMessage sent by a user attempting to delete their own user account.
     */
    REMOVE("RMV"),

    /**
     * ChatterMessage sent by the user attempting to create a chat group.
	 */
	CREATE("CRT"),

	/**
	 * ChatterMessage sent by the user attempting to join a chat group.
	 */
	JOIN("JIN"),

	/**
	 * ChatterMessage sent by the user attempting to send message to a certain group.
	 */
	GROUP("GRP"),

	/**
	 * ChatterMessage sent by the user attempting to send message to a certain user.
	 */
	PRIVATE("PVT"),

	/**
	 * ChatterMessage sent by the user attempting to delete certain group.
	 */
	DELETE("DEL"),

	/**
	 * ChatterMessage sent by the user attempting to leave a group.
	 */
	LEAVE("LEV"),

	/**
	 * Message sent by the user attempting to login using a specified username.
	 */
	HELLO("HLO"),

	/** Message sent by the server acknowledging a successful log in. */
	ACKNOWLEDGE("ACK"),

	/** Message sent by the server rejecting a login attempt. */
	NO_ACKNOWLEDGE("NAK"),

	/**
	 * Message sent by the user to start the logging out process and sent by the
	 * server once the logout process completes.
	 */
	QUIT("BYE"),

	/** Message whose contents is broadcast to all connected users. */
	BROADCAST("BCT");


		/** Store the short name of this message type. */
		private String tla;

		/**
		 * Define the message type and specify its short name.
		 * 
		 * @param abbrev Short name of this message type, as a String.
		 */
		private MessageType(String abbrev) {
			tla = abbrev;
		}

		/**
		 * Return a representation of this Message as a String.
		 * 
		 * @return Three letter abbreviation for this type of message.
		 */
		@Override
		public String toString() {
			return tla;
		}
	}

	/** The string sent when a field is null. */
	private static final String NULL_OUTPUT = "--";

	public String getMsgType() {
		return msgType.toString();
	}

	/** The handle of the message. */
	private MessageType msgType;

	/**
	 * The first argument used in the message. This will be the sender's identifier.
	 */
	private String msgSender;

	/** The second argument used in the message. */
	private String msgText;

	/** The time this message was sent. */
	private Date date;

	/** Prints the current time in the form of a string. */
	public void printtime() {
		System.out.println(date.toString());
	}

    /** Gets the saved date for this message.
     *
     * @return the stored daye
     */
	public Date gettime() {
		return date;
	}







	private SocketAddress IPaddress;
	public void setIPaddress(SocketAddress ip ) {  IPaddress=ip; }
	public SocketAddress getIPaddress (){return IPaddress;}


	/**
	 * Create a new message that contains actual IM text. The type of distribution
	 * is defined by the handle and we must also set the name of the message sender,
	 * message recipient, and the text to send.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Name of the individual sending this message
	 * @param text    Text of the instant message
	 */
	private Message(MessageType handle, String srcName, String text) {
		msgType = handle;
		// Save the properly formatted identifier for the user sending the
		// message.
		msgSender = srcName;
		// Save the text of the message.
		msgText = text;

        date = new Date();

	}

	/**
	 * Create simple command type message that does not include any data.
	 * 
	 * @param handle Handle for the type of message being created.
	 */
	private Message(MessageType handle) {
		this(handle, null, null);
	}

	/**
	 * Create a new message that contains a command sent the server that requires a
	 * single argument. This message contains the given handle and the single
	 * argument.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Argument for the message; at present this is the name used to
	 *                log-in to the IM server.
	 */
	private Message(MessageType handle, String srcName) {
		this(handle, srcName, null);
	}

	/**
	 * Create a new message to continue the logout process.
	 * 
	 * @return Instance of Message that specifies the process is logging out.
	 */
	public static Message makeQuitMessage(String myName) {
		return new Message(MessageType.QUIT, myName, null);
	}

	/**
	 * Create a new message broadcasting an announcement to the world.
	 * 
	 * @param myName Name of the sender of this very important missive.
	 * @param text   Text of the message that will be sent to all users
	 * @return Instance of Message that transmits text to all logged in users.
	 */
	public static Message makeBroadcastMessage(String myName, String text) {
		return new Message(MessageType.BROADCAST, myName, text);
	}

	/**
	 * Create a new message stating the name with which the user would like to
	 * login.
	 * 
	 * @param text Name the user wishes to use as their screen name.
	 * @return Instance of Message that can be sent to the server to try and login.
	 */
	protected static Message makeHelloMessage(String text) {
		return new Message(MessageType.HELLO, null, text);
	}

	/**
	 * Given a handle, name and text, return the appropriate message instance or an
	 * instance from a subclass of message.
	 * 
	 * @param handle  Handle of the message to be generated.
	 * @param srcName Name of the originator of the message (may be null)
	 * @param text    Text sent in this message (may be null)
	 * @return Instance of Message (or its subclasses) representing the handle,
	 *         name, & text.
	 */
	protected static Message makeMessage(String handle, String srcName, String text) {
		Message result = null;
		if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
			result = makeQuitMessage(srcName);
		} else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
			result = makeSimpleLoginMessage(srcName);
		} else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
			result = makeBroadcastMessage(srcName, text);
		} else if (handle.compareTo(MessageType.ACKNOWLEDGE.toString()) == 0) {
			result = makeAcknowledgeMessage(srcName);
		} else if (handle.compareTo(MessageType.NO_ACKNOWLEDGE.toString()) == 0) {
			result = makeNoAcknowledgeMessage();
		} else if (handle.compareTo(MessageType.LOGIN.toString()) == 0) {
            result = makeLoginMessage(srcName, text);
        } else if (handle.compareTo(MessageType.REMOVE.toString()) == 0) {
            result = makeRemoveMessage(srcName);
		} else if (handle.compareTo(MessageType.CREATE.toString()) == 0) {
			result = makeCreateMessage(srcName, text);
		} else if (handle.compareTo(MessageType.JOIN.toString()) == 0) {
			result = makeJoinMessage(srcName, text);
		} else if (handle.compareTo(MessageType.DELETE.toString()) == 0) {
			result = makeDeleteMessage(srcName, text);
		} else if (handle.compareTo(MessageType.LEAVE.toString()) == 0) {
			result = makeLeaveMessage(srcName, text);
		} else if (handle.compareTo(MessageType.GROUP.toString()) == 0) {
			result = makeGroupMessage(srcName, text);
		} else if (handle.compareTo(MessageType.PRIVATE.toString()) == 0) {
			result = makePrivateMessage(srcName, text);
		} else if (handle.compareTo(MessageType.REGISTER.toString()) == 0) {
			result = makeRegisterMessage(srcName, text);
		}
		return result;
	}

	/**
	 * Create a new message create a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param groupName   name of group user want to create
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makeCreateMessage(String myName, String groupName) {
		return new Message(MessageType.CREATE, myName, groupName);
	}

    /**
     * Delete an entire user account.
     */
    public static Message makeRemoveMessage(String myName) {
        return new Message(MessageType.REMOVE, myName);
    }
	/**
	 * Create a new message join a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param groupName   name of group user want to join
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makeJoinMessage(String myName, String groupName) {
		return new Message(MessageType.JOIN, myName, groupName);
	}

	/**
	 * Create a new message delete a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param groupName   name of group user want to delete
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makeDeleteMessage(String myName, String groupName) {
		return new Message(MessageType.DELETE, myName, groupName);
	}

	/**
	 * Create a new message leave a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param groupName   name of group user want to leave
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makeLeaveMessage(String myName, String groupName) {
		return new Message(MessageType.LEAVE, myName, groupName);
	}

	/**
	 * Create a new message sending to a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param groupText   name of group user want to send combine with text user want to send into this group
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makeGroupMessage(String myName, String groupText) {
		return new Message(MessageType.GROUP, myName, groupText);
	}

	/**
	 * Create a new message leave a group.
	 *
	 * @param myName Name of the sender of this very important missive.
	 * @param privateText   name of group user want to send combine with text user want to send into this group
	 * @return Instance of ChatterMessage that transmits text to all logged in users.
	 */
	public static Message makePrivateMessage(String myName, String privateText) {
		return new Message(MessageType.PRIVATE, myName, privateText);
	}

	/**
	 * Create a new message for the early stages when the user logs in with
	 * username and password
	 *
	 * @param text Name of the user who has just logged in.
	 * @return Instance of ChatterMessage specifying a new friend has just logged in.
	 */
	public static Message makeLoginMessage(String myName, String text) {
		return new Message(MessageType.LOGIN, myName, text);
	}

	/**
	 * Create a new message for the early stages when the user register with
	 * username and password
	 *
	 * @param text Name of the user who has just logged in.
	 * @return Instance of ChatterMessage specifying a new friend has just logged in.
	 */
	public static Message makeRegisterMessage(String myName, String text) {
		return new Message(MessageType.REGISTER, myName, text);
	}

	/**
	 * Create a new message to reject the bad login attempt.
	 * 
	 * @return Instance of Message that rejects the bad login attempt.
	 */
	public static Message makeNoAcknowledgeMessage() {
		return new Message(MessageType.NO_ACKNOWLEDGE);
	}

	/**
	 * Create a new message to acknowledge that the user successfully logged as the
	 * name <code>srcName</code>.
	 * 
	 * @param srcName Name the user was able to use to log in.
	 * @return Instance of Message that acknowledges the successful login.
	 */
	public static Message makeAcknowledgeMessage(String srcName) {
		return new Message(MessageType.ACKNOWLEDGE, srcName);
	}

	/**
	 * Create a new message for the early stages when the user logs in without all
	 * the special stuff.
	 * 
	 * @param myName Name of the user who has just logged in.
	 * @return Instance of Message specifying a new friend has just logged in.
	 */
	public static Message makeSimpleLoginMessage(String myName) {
		return new Message(MessageType.HELLO, myName);
	}

	/**
	 * Return the name of the sender of this message.
	 * 
	 * @return String specifying the name of the message originator.
	 */
	public String getName() {
		return msgSender;
	}

	/**
	 * Return the text of this message.
	 * 
	 * @return String equal to the text sent by this message.
	 */
	public String getText() {
		return msgText;
	}

	/**
	 * Determine if this message is an acknowledgement message.
	 * 
	 * @return True if the message is an acknowledgement message; false otherwise.
	 */
	public boolean isAcknowledge() {
		return (msgType == MessageType.ACKNOWLEDGE);
	}

	/**
	 * Determine if this message is broadcasting text to everyone.
	 * 
	 * @return True if the message is a broadcast message; false otherwise.
	 */
	public boolean isBroadcastMessage() {
		return (msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message contains text which the recipient should display.
	 * 
	 * @return True if the message is an actual instant message; false if the
	 *         message contains data
	 */
	public boolean isDisplayMessage() {
		return (msgType == MessageType.LOGIN ||
				msgType == MessageType.REGISTER ||
                msgType == MessageType.REMOVE ||
				msgType == MessageType.CREATE ||
				msgType == MessageType.JOIN ||
				msgType == MessageType.GROUP ||
				msgType == MessageType.PRIVATE ||
				msgType == MessageType.DELETE ||
				msgType == MessageType.LEAVE ||
				msgType == MessageType.HELLO ||
				msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message is sent by a new client to log-in to the server.
	 * 
	 * @return True if the message is an initialization message; false otherwise
	 */
	public boolean isInitialization() {
		return (msgType == MessageType.HELLO);
	}

	/**
	 * Determine if this message is a message signing off from the IM server.
	 * 
	 * @return True if the message is sent when signing off; false otherwise
	 */
	public boolean terminate() {
		return (msgType == MessageType.QUIT);
	}

	/**
	 * Representation of this message as a String. This begins with the message
	 * handle and then contains the length (as an integer) and the value of the next
	 * two arguments.
	 * 
	 * @return Representation of this message as a String.
	 */
	@Override
	public String toString() {
		String result = msgType.toString();
		if (msgSender != null) {
			result += " " + msgSender.length() + " " + msgSender;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		if (msgText != null) {
			result += " " + msgText.length() + " " + msgText;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		return result;
	}
}
