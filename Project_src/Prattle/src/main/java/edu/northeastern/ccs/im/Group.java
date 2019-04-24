package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.Prattle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * a group class, with users and group message histories
 */
public class Group implements Serializable {

    private static final long serialVersionUID = -1013638914053844279L;

    /**
     * name
     */
    private String GroupName;

    /**
     * user list
     */
    private ArrayList<String> UserList = new ArrayList<>(5);

    /**
     * message history list
     */
    private ArrayList<Message> MessageQueue = new ArrayList<Message>(50);


    /**
     * constructor
     *
     * @param aGroupName
     */
    public Group(String aGroupName) {
        GroupName = aGroupName;
    }


    /**
     * add a user to this group
     *
     * @param username
     */
    public void addUser(String username) {
        UserList.add(username);

    }

    /**
     * remove a user from this group
     *
     * @param username
     */
    public void removeUser(String username) {
        UserList.remove(username);
    }

    private boolean debug = false;

    public ArrayList<String> getUserList() {
        return UserList;
    }

    public void setUserList(ArrayList<String> userList) {
        UserList = userList;
    }

    public ArrayList<Message> getMessageQueue() {
        return MessageQueue;
    }

    public void setMessageQueue(ArrayList<Message> messageQueue) {
        MessageQueue = messageQueue;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * broadcast message to each user in this group
     *
     * @param msg
     */
    public void groupBroadcastMessage(Message msg) throws debugException {
        for (String uu : UserList) {
            if(debug) {
                throw new debugException(msg.toString());
            } else {
                Prattle.sendMessage(uu, msg);
            }
        }
        MessageQueue.add(msg);

    }


    /**
     * return a list of messages that are stored after the argument time
     *
     * @param time
     * @return
     */
    public List<Message> messagesSinceTime(Date time) {

        List<Message> msglist = new ArrayList<Message>(10);

        for (Message msg : MessageQueue) {
            if (msg.gettime().compareTo(time) >= 0) {
                msglist.add(msg);
            }
        }
        return msglist;
    }


    /**
     * return group msg
     *
     * @return group msg
     */
    public ArrayList<String> returnUserQueue() {
        return UserList;
    }

    /**
     * return group name
     *
     * @return group name
     */
    public String returnGroupName() {
        return GroupName;
    }


    /**
     * change group name
     *
     * @param newName
     */
    public void changeGroupName(String newName) {
        GroupName = newName;
    }

    public boolean hasUser(String username) {
        boolean result = false;
        for(String user : UserList) {
            if(user.equals(username)) {
                result = true;
            }
        }
        return result;
    }

    public class debugException extends Exception {
        public debugException(String msg) {
            super(msg);
        }
    }

}