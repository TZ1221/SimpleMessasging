package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import static edu.northeastern.ccs.im.SocketNBTest.LOCAL_HOST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * testing ScanNetNB
 */
public class ScanNetNBTest {
    /**
     * testing constructor
     * @throws IOException
     */
    @Test
    public void testSocketNBCreation() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(4343));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);


        SocketChannel serversideclientsocket = serverSocket.accept();

        SocketNB snb = new SocketNB("localhost", 4343);
        ScanNetNB snnb = new ScanNetNB(snb);
        snnb.close();
        serverSocket.close();
    }


    /**
     * testing NextMessage()
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void testhasNextMessage() throws IOException, InterruptedException {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(6363));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 6363);
        SocketChannel serversideclientsocket = serverSocket.accept();


        PrintNetNB clientPrinter = new PrintNetNB(serversideclientsocket);

        ScanNetNB clientScanner = new ScanNetNB(client);
        Thread.sleep(3000);
        //no message
        assertFalse(clientScanner.hasNextMessage() );
        System.err.flush();




        Message hello = Message.makeSimpleLoginMessage("guy");
        clientPrinter.print(hello);
        clientPrinter.print(Message.makeSimpleLoginMessage(""));
        //with message
        assertTrue(clientScanner.hasNextMessage());



        client.close();
        serverSocket.close();

    }


    /**
     * testing NextMessage
     * @throws IOException
     * @throws InterruptedException
     * @throws NextDoesNotExistException
     */
    @Test
    void testNextMsg() throws IOException, InterruptedException,NextDoesNotExistException  {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(7363));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 7363);
        SocketChannel serversideclientsocket = serverSocket.accept();


        PrintNetNB clientPrinter = new PrintNetNB(serversideclientsocket);
        ScanNetNB clientScanner = new ScanNetNB(client);
        Thread.sleep(3000);



        boolean thrown = false;
        try {
            clientScanner.nextMessage();
        } catch (NextDoesNotExistException e) {
            thrown = true;
        }

        assertTrue(thrown);
        System.err.flush();


        client.close();
        serverSocket.close();

    }






}
