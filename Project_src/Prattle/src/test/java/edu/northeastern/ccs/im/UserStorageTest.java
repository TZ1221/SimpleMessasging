
package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/** A class to test the user storage. */


class UserStorageTest {


/** Variables for testing. */

    private String username = "testUser";
    private String password = "1234";
    private UserStorage user = new UserStorage(username, password);


/** Test logout. */

    @Test
    void logout() {
        Date oldDate = new Date();
        user.logout();
        Date checkDate = user.getLastLogin();
        assertEquals(oldDate, checkDate);
    }


/** Test get name returns the set username. */

    @Test
    void getName() {
        assertEquals(username, user.getName());
    }


/** Test update name correctly assigns a new username. */

    @Test
    void updateName() {
        String newName = "testName";
        user.updateName(newName);
        String checkName = user.getName();
        assertNotEquals(username, checkName);
        assertEquals(newName, checkName);
    }


/** Test get password returns the set password. */

    @Test
    void getPassword() {
        assertEquals(password, user.getPassword());
    }


/** Test update password correctly assigns a new password. */

    @Test
    void updatePassword() {
        String newPassword = "5678";
        user.updatePassword(newPassword);
        assertEquals(newPassword, user.getPassword());
    }


/** Check the last login date. */

    @Test
    void getLastLogin() {
        Date oldDate = new Date();
        user.logout();
        Date checkDate = user.getLastLogin();
        assertEquals(oldDate, checkDate);
    }


/** Test adding a group correctly adds it to the groups list. */

    @Test
    void addGroup() {
        String group = "groupName";
        user.addGroup(group);
        assertTrue(user.getGroups().contains(group));
    }


/** Test removing a group correctly removes it from the groups list. */

    @Test
    void removeGroup() {
        String group = "otherGroup";
        user.addGroup(group);
        user.removeGroup(group);
        assertFalse(user.getGroups().contains(group));
    }


/** Test getMessages correctly returns a list of unread messages. */

    @Test
    void getMessages() {
        String testMessage = "test message";
        String name = "username";
        String hello = "HLO";
        Message message = Message.makeMessage(hello, name, testMessage);
        user.addOfflineMessage(message);
        assertEquals(1, user.getMessages().size());
        assertTrue(user.getMessages().contains(message));
    }


/** Test addMessage correctly adds a message to the history. */

    @Test
    void addMessage() {
        String testMessage = "test message again";
        String name = "username";
        String hello = "HLO";
        Message message = Message.makeMessage(hello, name, testMessage);
        user.addMessage(message);
        user.addMessage(message);
        user.addMessage(message);
        assertEquals(3, user.getMessageHistory().size());
    }


/** Test adding an offline message to the unreadMessage queue. */

    @Test
    void addOfflineMessage() {
        String testMessage = "another test";
        String name = "username";
        String hello = "HLO";
        Message message = Message.makeMessage(hello, name, testMessage);
        user.addOfflineMessage(message);
        user.addOfflineMessage(message);
        user.addOfflineMessage(message);
        assertEquals(3, user.getMessages().size());
    }


/** Test that the message history returns all messages sent to this user. */
    @Test
    void getMessageHistory() {
        String testMessage = "last test";
        String name = "username";
        String hello = "HLO";
        Message message = Message.makeMessage(hello, name, testMessage);
        user.addMessage(message);
        assertEquals(1, user.getMessageHistory().size());
        assertTrue(user.getMessageHistory().contains(message));
    }
}
