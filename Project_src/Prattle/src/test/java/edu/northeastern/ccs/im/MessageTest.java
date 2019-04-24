package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class to create various messages. */
class MessageTest {

    /**
     * Coverage for MakeHelloMessage, which is the only message not
     * called by the generic "makeMessage" method.
     */
    @Test
    void makeHelloMessage() {
        String checkString = "HLO 2 -- 10 test hello";
        String helloMessage = "test hello";
        Message message = Message.makeHelloMessage(helloMessage);
        assertEquals(checkString, message.toString());
        assertNull(message.getName());
        assertEquals(helloMessage, message.getText());
        assertTrue(message.isInitialization());
    }

    /** Test various messages created by makeMessage. */
    @Test
    void makeMessage() {
        /** Variables for testing. */
        String testMessage = "test message";
        String user = "username";
        String hello = "HLO";
        String broadcast = "BCT";
        String quit = "BYE";
        String ack = "ACK";
        String noAck = "NAK";
        String fullMessage = hello + " 8 " + user + " 2 --";
        Message message = Message.makeMessage(hello, user, testMessage);
        assertEquals(fullMessage, message.toString());
        assertEquals(user, message.getName());
        assertNull(message.getText());
        assertTrue(message.isInitialization());
        assertFalse(message.isAcknowledge());
        assertFalse(message.isBroadcastMessage());
        message.printtime();

        /** Create a Broadcast message. */
        fullMessage = broadcast + " 8 " + user + " 12 test message";
        message = Message.makeMessage(broadcast, user, testMessage);
        assertEquals(fullMessage, message.toString());
        assertEquals(user, message.getName());
        assertEquals(testMessage, message.getText());
        assertTrue(message.isBroadcastMessage());
        assertTrue(message.isDisplayMessage());
        assertFalse(message.isInitialization());
        message.printtime();

        /** Create an Acknowledgemet message. */
        fullMessage = ack + " 8 " + user + " 2 --";
        message = Message.makeMessage(ack, user, testMessage);
        assertEquals(fullMessage, message.toString());
        assertEquals(user, message.getName());
        assertNull(message.getText());
        assertTrue(message.isAcknowledge());
        assertFalse(message.terminate());
        message.printtime();

        /** Create a No Acknowledgement message. */
        fullMessage = noAck + " 2 -- 2 --";
        message = Message.makeMessage(noAck, user, testMessage);
        assertEquals(fullMessage, message.toString());
        assertNull(message.getName());
        assertNull(message.getText());
        message.printtime();

        /** Create a termination message. */
        fullMessage = quit + " 8 " + user + " 2 --";
        message = Message.makeMessage(quit, user, testMessage);
        assertEquals(fullMessage, message.toString());
        assertEquals(user, message.getName());
        assertNull(message.getText());
        assertTrue(message.terminate());
        message.printtime();



        /** no match*/
        message = Message.makeMessage("testing", user, testMessage);
        assertNull(message);




    }

    @Test
    public void testMessageEnum() {
        for(Message.MessageType type : Message.MessageType.values()) {
            Message msg = Message.makeMessage(type.toString(), "messageenumtest", "hey");
            assertNotNull(msg);
            if(msg.isDisplayMessage()) {
                assertTrue(msg.isDisplayMessage());

            }else {
                assertFalse(msg.isDisplayMessage());
            }

        }
    }

}