package edu.northeastern.ccs.im;

import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.server.Prattle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Class to test Client Runnable
 */
public class ClientRunnableTest {
    /**
     * Stores the client
     */
    ClientRunnable client1;

    /**
     * Stores the socketCHannel for the server
     */
    SocketChannel socketChannel;

    /**
     * Scanning the Client
     */
    ScanNetNB scanNetNB;

    /**
     * Printing from the client
     */
    PrintNetNB printNetNB;


    /**
     * initialize ClientRunnableTest
     */
    ClientRunnableTest() throws Exception {
        socketChannel = SocketChannel.open();
        client1 = new ClientRunnable(socketChannel);
        scanNetNB = mock(ScanNetNB.class);
        printNetNB = mock(PrintNetNB.class);

    }

    /**
     * test for broadcast Message
     */
    @Test
    void runWithBroadcastMessageTest() throws Exception {

        Prattle.startUp();



        Field initialized = client1.getClass().getDeclaredField("initialized");
        Field input = client1.getClass().getDeclaredField("input");
        Field output = client1.getClass().getDeclaredField("output");

        initialized.setAccessible(true);
        input.setAccessible(true);
        output.setAccessible(true);

        initialized.set(client1, false);
        when(scanNetNB.hasNextMessage()).thenReturn(true);
        Message msg = Message.makeBroadcastMessage("Mandy", "test");
        when(scanNetNB.nextMessage()).thenReturn(msg);

        input.set(client1, scanNetNB);


        client1.run();
        assertEquals(true, client1.isInitialized());
        client1.run();
        Message msgLogOff = Message.makeBroadcastMessage("Mandy", "Prattle says everyone log off");
        when(scanNetNB.nextMessage()).thenReturn(msgLogOff);
        client1.run();

        Message msgWithDiffName = Message.makeBroadcastMessage("Noodle", "Hello from Noodle");
        client1.setName("Mandy");
        System.out.print(client1.getName());
        System.out.print(msgWithDiffName.getName());
        client1.run();

        assertEquals(true, client1.isInitialized());
        when(scanNetNB.hasNextMessage()).thenReturn(true);
        when(scanNetNB.nextMessage()).thenReturn(msgWithDiffName);
        input.set(client1, scanNetNB);
        output.set(client1, printNetNB);

        ClientRunnable clientRunnable = spy(client1);
        Mockito.doNothing().when(clientRunnable).terminateClient();
        clientRunnable.run();





    }

/*    *//**
     * test for terminateclient without throw exception
     *//*
    @Test
    void terminateClientTest() throws Exception {
        try {
            client1.terminateClient();
            verify(scanNetNB, times(1)).close();
        } catch (NullPointerException ex) {
        }
    }*/

    /**
     * test for quit message
     */
    @Test
    void runWithQuitMessageTest() throws Exception {
        Message quitMessage = Message.makeQuitMessage("Mandy");
        Field input = client1.getClass().getDeclaredField("input");
        Field initialized = client1.getClass().getDeclaredField("initialized");
        Field output = client1.getClass().getDeclaredField("output");
        output.setAccessible(true);
        output.set(client1, printNetNB);
        initialized.setAccessible(true);
        initialized.set(client1, false);
        input.setAccessible(true);
        input.set(client1, scanNetNB);
        Mockito.when(scanNetNB.hasNextMessage()).thenReturn(true);
        Mockito.when(scanNetNB.nextMessage()).thenReturn(quitMessage);
        ClientRunnable spy = spy(client1);
        Mockito.doNothing().when(spy).terminateClient();
        spy.run();
        assertEquals(true, spy.isInitialized());
        spy.run();
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        verify(spy, times(1)).enqueueMessage(argument.capture());
        assertEquals("Mandy", argument.getValue().getName());
    }

  /*  *//**
     * test when terminateClient function throws exception
     *//*
    @Test
    void terminateClientFailTest() throws Exception {
        ScanNetNB scanNetNB1 = mock(ScanNetNB.class);
        try {
            ClientRunnable client2 = new ClientRunnable(socketChannel);
            client2.terminateClient();
            doThrow(new IOException()).when(scanNetNB1).close();

            Throwable e = assertThrows(IOException.class, () -> {
                scanNetNB1.close();
            });
            verify(scanNetNB1, times(1)).close();
            assertEquals("Could not find a thread that I tried to remove!", e.getMessage());
        } catch (NullPointerException ex) {
        }
    }*/


    /**
     * test for special broadcast message
     */
    @Test
    void runWithRequestTest() throws Exception {
        Message msg = Message.makeBroadcastMessage("Mandy","What time is it Mr. Fox?");
        client1.setName("Mandy");
        Queue<Message> queue = new ArrayDeque<>();
        Field field = client1.getClass().getDeclaredField("specialResponse");
        field.setAccessible(true);

        Field initialized = client1.getClass().getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(client1, true);

        field.set(client1, queue);
        Field input = client1.getClass().getDeclaredField("input");
        input.setAccessible(true);
        input.set(client1, scanNetNB);
        when(scanNetNB.hasNextMessage()).thenReturn(true);

        when(scanNetNB.nextMessage()).thenReturn(msg);
        client1.run();
        assertTrue(client1.isInitialized());
    }

    /**
     * test for getUserId
     */
    @Test
    void getUserIdTest() {
        assertEquals(0, client1.getUserId());
    }

    /**
     * test when username in message is null
     */
    @Test
    void usernameWithNullTest() throws Exception {
        Field initialized = client1.getClass().getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(client1, false);

        Field input = client1.getClass().getDeclaredField("input");
        input.setAccessible(true);
        input.set(client1, scanNetNB);

        Message msgNullName = Message.makeBroadcastMessage(null, "no Name Message");
        when(scanNetNB.hasNextMessage()).thenReturn(true);

        when(scanNetNB.nextMessage()).thenReturn(msgNullName);
        client1.run();
        assertFalse(client1.isInitialized());
    }

    @Test
    public void handleInboundClientMessageTest() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException{
        Prattle.startUp();
        Field output = client1.getClass().getDeclaredField("output");
        output.setAccessible(true);
        output.set(client1, printNetNB);


        ClientRunnable client = new ClientRunnable(SocketChannel.open());
        client.setName("testRegister");
        client.setWaitingList(new ConcurrentLinkedQueue<>());
        client.setDebug(true);
        Prattle.addClient(client);

        //Set prattle debug to true to be able to return testUser and testGroup
        Prattle.setDebug(true);
        //Set testUser
        Prattle.setTestUser(new UserStorage("loginTest", "password"));



        Message registerMessage = Message.makeRegisterMessage("testRegister", "password");

        ClientRunnable seperateLogin = new ClientRunnable(SocketChannel.open());
        seperateLogin.setName("seperateLogin");
        seperateLogin.setWaitingList(new ConcurrentLinkedQueue<>());
        seperateLogin.setDebug(true);
        Prattle.addClient(seperateLogin);

        //Set prattle debug to true to be able to return testUser and testGroup
        Prattle.setDebug(true);
        //Set testUser
        Prattle.setTestUser(new UserStorage("loginTest", "password"));

        Prattle.register("seperateLogin", "password");

        Message seperateLoginMessage = Message.makeLoginMessage("seperateLogin", "password");
        Message privateMessageFail = Message.makeMessage(Message.MessageType.PRIVATE.toString(), "anything", "private loginTest message");

        Message failedLogin = Message.makeLoginMessage("testRegister", "incorrectpassword");

        Message removeUser = Message.makeRemoveMessage("seperateLogin");

        Message registerFail = Message.makeRegisterMessage("testRegister", "password");

        Message sendGroup = Message.makeMessage(Message.MessageType.GROUP.toString(), "seperateLogin", "group groupname message");

        Message sendGroupFail = Message.makeMessage(Message.MessageType.GROUP.toString(), "seperateLogin", "group notfound message");

        Message privateMessage =Message.makeMessage(Message.MessageType.PRIVATE.toString(), "seperateLogin", "private loginTest message");


        //registers testregister
        client1.handleInboundClientMessage(registerMessage);

        // seperate login
        client1.handleInboundClientMessage(seperateLoginMessage);

        //failed login
        client1.handleInboundClientMessage(failedLogin);

        //failed register
        client1.handleInboundClientMessage(registerFail);


        //private message
        client1.handleInboundClientMessage(privateMessage);

        //private message fail
        client1.handleInboundClientMessage(privateMessageFail);

        Message createGroupMessage = Message.makeCreateMessage("testRegister", "groupname");
        //create group
        client1.handleInboundClientMessage(createGroupMessage);


        //creat group fail
        client1.handleInboundClientMessage(createGroupMessage);

        Message joinGroup = Message.makeJoinMessage("testRegister", "groupname");
        //join group
        client1.handleInboundClientMessage(joinGroup);

        Message joinGroupFail = Message.makeJoinMessage("testRegister", "notagroup");
        //Join group fail
        client1.handleInboundClientMessage(joinGroupFail);

        //send group
        client1.handleInboundClientMessage(sendGroup);
        //send group fail

        client1.handleInboundClientMessage(sendGroupFail);


        Message leaveGroup = Message.makeLeaveMessage("testRegister", "groupname");
        //leave group
        client1.handleInboundClientMessage(leaveGroup);

        Message leaveGroupFail = Message.makeLeaveMessage("testRegister", "unknowngroup");
        //leave group fail
        client1.handleInboundClientMessage(leaveGroupFail);

        Message deleteGroup = Message.makeDeleteMessage("testRegister", "groupname");
        //delete group
        client1.handleInboundClientMessage(deleteGroup);

        Message deleteGroupFail = Message.makeDeleteMessage("testRegister", "unknown");
        //delete group fail
        client1.handleInboundClientMessage(deleteGroupFail);



        //remove user seperatelogin
        client1.handleInboundClientMessage(removeUser);



    }


}
