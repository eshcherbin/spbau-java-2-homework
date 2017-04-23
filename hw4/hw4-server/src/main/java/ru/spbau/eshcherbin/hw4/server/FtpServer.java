package ru.spbau.eshcherbin.hw4.server;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class FtpServer implements Server {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(FtpServer.class);
    private @NotNull Thread serverThread;
    private volatile boolean isRunning = false;

    /**
     * Creates and configures the server.
     * @param bindingAddress the address to which this server is bound.
     */
    public FtpServer(@NotNull SocketAddress bindingAddress) {
        serverThread = new Thread(new FtpServerConnectionAccepter(bindingAddress));
    }

    /**
     * Starts the server.
     */
    @Override
    public void start() throws IOException {
        isRunning = true;
        serverThread.start();
        logger.info("Server started");
    }

    /**
     * Stops the server.
     */
    @Override
    public void stop() {
        isRunning = false;
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Server stopped");
    }

    private class FtpServerConnectionAccepter implements Runnable {
        private static final long SELECT_TIMEOUT = 500;
        private @NotNull SocketAddress bindingAddress;
        
        public FtpServerConnectionAccepter(@NotNull SocketAddress bindingAddress) {
            this.bindingAddress = bindingAddress;
        }

        /**
         * Performs all the server logic.
         */
        @Override
        public void run() {
            try (Selector selector = Selector.open()) {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.bind(bindingAddress);
                serverChannel.configureBlocking(false);
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                
                while (isRunning) {
                    selector.select(SELECT_TIMEOUT);
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();
                        if (selectionKey.isAcceptable()) {
                            handleAccept(selectionKey);
                        } else if (selectionKey.isReadable()) {
                            handleIncoming(selectionKey);
                        } else if (selectionKey.isWritable()) {
                            handleOutgoing(selectionKey);
                        }
                        keyIterator.remove();
                    }
                }
                
            } catch (IOException e) {
                logger.error("I/O error while running server: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleOutgoing(@NotNull SelectionKey selectionKey) {
            //TODO: handle outgoing
            throw new NotImplementedException();
        }

        private void handleIncoming(@NotNull SelectionKey selectionKey) {
            //TODO: handle incoming
            throw new NotImplementedException();
        }
        
        private void handleAccept(@NotNull SelectionKey selectionKey) throws IOException {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverChannel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
            }
        }
    }
}
