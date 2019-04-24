package edu.northeastern.ccs.im;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Test class for SocketNB, a non-binding socket */

public class SocketNBTest {

    public static final String LOCAL_HOST = "localhost";

    /**
     * Open a server end and close it
     */
    @Test
    public void testClose() throws IOException {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(4848));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 4848 );
        client.close();
        serverSocket.close();
    }

    /**
     * test whether SocketNB built correctly
     */
    @Test
    public void testGetSocket() throws IOException {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(5757));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);


        SocketNB client = new SocketNB(LOCAL_HOST, 5757);
        assertTrue(client.getSocket() instanceof SocketChannel);
        client.close();
        serverSocket.close();
    }


    /**
     * open a server and bind with client
     */
    @Test
    public void testSocketNBCreation() throws IOException {

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(4747));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 4747);
        assertNotNull(client);
        assertTrue(client instanceof SocketNB);
        client.close();
        serverSocket.close();
    }

}