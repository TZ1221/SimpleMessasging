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
import static org.junit.jupiter.api.Assertions.assertEquals;


/** Test class for PrintNetNB. */

class PrintNetNBTest {

    /** Test the print functionality of PrintNetNB.
     * @throws IOException
     * @throws InterruptedException
     * */
    @Test
    void print() throws IOException, InterruptedException {

        /** Create and bind to new server */
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(5353));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 5353);

        SocketChannel serversideclientsocket = serverSocket.accept();


        /**Test printer by creating message, printing message, and scanning for message in client */
        PrintNetNB clientPrinter = new PrintNetNB(serversideclientsocket);
        Message hello = Message.makeSimpleLoginMessage("guy");
        clientPrinter.print(hello);

        ScanNetNB clientScanner = new ScanNetNB(client);
        Thread.sleep(3000);

        String help = null;

        if(clientScanner.hasNextMessage()) {
            help = clientScanner.nextMessage().toString();
        }
        
        System.err.flush();


        /*simpleServer.getMessage();*/
        assertEquals(hello.toString(), help);
        client.close();
        serverSocket.close();

    }

    /** Check alternate constructor of PrintNetNB.
     * @throws IOException
     * */
    @Test
    public void testAlternateConstructor() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(5252));
        // Create the Selector with which our channel is registered.
        Selector selector = SelectorProvider.provider().openSelector();
        // Register to receive any incoming connection messages.
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        SocketNB client = new SocketNB(LOCAL_HOST, 5252);

        SocketChannel serversideclientsocket = serverSocket.accept();

        PrintNetNB printerTest = new PrintNetNB(client);

        client.close();
        serverSocket.close();
    }


}