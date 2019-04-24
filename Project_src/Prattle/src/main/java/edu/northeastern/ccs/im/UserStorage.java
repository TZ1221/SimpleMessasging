package edu.northeastern.ccs.im;

import java.io.Serializable;
import java.util.*;

/**
 * A class to represent the notion of individual users
 * to login to and be stored by the server.
 */
public class UserStorage implements Serializable {

    /**
     * The unique username to represent the user. Uniqueness must be
     * checked by the server on creation.
     */
    private String username;

    /** The stored password. Encryption handled by client. */
    private String encryptedPassword;

    /** The list of messages this user has received. */
    private Queue<Message> messageHistory;

    /** Unread messages logged while this user was offline. */
    private Queue<Message> unreadMessages;

    /** The user's last login date(the time they last logged out).*/
    private Date lastLogin;

    /** A list of this user's groups. */
    private List<String> memberGroups;

    /**
     * Constructor for UserStorage upon creation of a new User. Stores
     * the username and password and creates all associated fields.
     *
     * @param user the unique username. uniqueness must be checked by server.
     * @param password the password
     */
    public UserStorage(String user, String password){
        username = user;
        encryptedPassword = password;
        messageHistory = new LinkedList<>();
        unreadMessages = new LinkedList<>();
        memberGroups = new ArrayList<>(200);
    }

    /**
     * Called when the user is logged out. Sets the last login
     * date to the current time of log out.
     */
    public void logout(){ lastLogin = new Date(); }

    /**
     * Get the stored username.
     *
     * @return the username
     */
    public String getName() { return username; }

    /**
     * Update the username. New username availability must be checked by server.
     *
     * @param newName the new username
     */
    public void updateName(String newName) { username = newName; }

    /**
     * The stored password associated with this username.
     *
     * @return the password
     */
    public String getPassword() { return encryptedPassword; }

    /**
     * Update the user's stored password.
     *
     * @param newPassword the new password to save
     */
    public void updatePassword(String newPassword){ encryptedPassword = newPassword; }

    /** The last login date of this user.
     *
     * @return the date/time this user was last logged in
     */
    public Date getLastLogin() { return lastLogin; }

    /**
     * When a user is added to a group, add record of this group to the user.
     *
     * @param groupName the group to add to the list
     */
    public void addGroup(String groupName) { memberGroups.add(groupName); }

    /**
     * When a user is removed from a group, also remove the group record.
     *
     * @param group the group to remove
     */
    public void removeGroup(String group) { memberGroups.remove(group) ; }

    /**
     * Return a list of groups the user is in.
     */
    public List<String> getGroups(){ return memberGroups; }

    /**
     * Get any unread messages since this user was last online.
     *
     * @return the unreadMessages queue
     */
    public Queue<Message> getMessages() { return unreadMessages; }

    /**
     * Add a message to the message history. Called whenever this user receives
     * a new message while online.
     *
     * @param message the message to add to the history
     */
    public void addMessage(Message message) { messageHistory.add(message); }

    /**
     * Add a message to the unreadMessages queue when this user is offline.
     * Also adds the message to the message history.
     *
     * @param message
     */
    public void addOfflineMessage(Message message) { unreadMessages.add(message); addMessage(message);}

    /**
     * Return this user's entire message history.
     */
    public Queue getMessageHistory() { return messageHistory; }
}
