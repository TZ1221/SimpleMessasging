package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * testing Group class
 */

public class GroupTest {

    Group g=  new Group( "1");
    String user="user1";
    String password;
    UserStorage u= new UserStorage(user, password);
    String user2="user2";
    String password2;
    UserStorage u2= new UserStorage(user2, password2);



    @Test
    void  theGroup () {
        assertNotNull(g);
        assertEquals(g.returnGroupName(), "1");
    }


    @Test
    void addUser() {
        g.addUser(user);
        assertEquals(user, g.getUserList().get(0));
    }



    @Test
    void removeUser() {
        assertTrue(g.returnUserQueue().isEmpty());
        g.addUser(user);
        assertEquals(g.returnUserQueue().size(), 1);
        g.removeUser(user);
        assertTrue(g.returnUserQueue().isEmpty());
    }



    @Test
    void returnUserQueue() {
        assertTrue(g.returnUserQueue().isEmpty());
        g.addUser(user);
        assertFalse(g.returnUserQueue().isEmpty());
    }


    @Test
    void returnGroupName() {
       assertEquals( g.returnGroupName(), "1");
    }



    @Test
    void changeGroupName() {
        g.changeGroupName("2");
        assertEquals( g.returnGroupName(), "2");
    }




    @Test
    void groupBroadcastMessage() {
        Group fakeGroup = new Group("groupBroadcastMessageTest");
        ArrayList<String> userList = new ArrayList<>(1);
        userList.add("TestUser");
        fakeGroup.setUserList(userList);
        fakeGroup.setDebug(true);

        Message testMessage = Message.makeMessage(Message.MessageType.PRIVATE.toString(), "TestUser", "testingfromGroupBroadcast");

        try{
            fakeGroup.groupBroadcastMessage(testMessage);
        } catch (Group.debugException e) {
            if(e.getMessage().equals(testMessage.toString())) {
                assert(true);
            } else {
                e.printStackTrace();
            }
        }


    }


    @Test
    void messageSinceTime() {

        Group messagesSinceTimeGroup = new Group("messagesSinceTimeGroup");
        ArrayList<Message> msgList = new ArrayList<>(10);
        Date middleDate = null;
        for(int i = 0; i < 5; i++) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = Message.makeHelloMessage("eh");
            if(i == 3) {
                middleDate = message.gettime();
            }
            msgList.add(message);
        }
       messagesSinceTimeGroup.setMessageQueue(msgList);
        assertEquals(messagesSinceTimeGroup.messagesSinceTime(middleDate).size(), 2);

    }



}
