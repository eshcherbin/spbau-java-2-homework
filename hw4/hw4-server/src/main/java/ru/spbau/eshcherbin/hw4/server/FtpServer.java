package ru.spbau.eshcherbin.hw4.server;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponseItem;
import ru.spbau.eshcherbin.hw4.ftp.FtpQuery;
import ru.spbau.eshcherbin.hw4.messages.Message;
import ru.spbau.eshcherbin.hw4.messages.MessageReader;
import ru.spbau.eshcherbin.hw4.messages.MessageWriter;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

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

        /**
         * Does the necessary work with a channel ready for writing.
         * @param selectionKey the channel's selection key
         * @throws IOException if an I/O error occurs
         */
        private void handleOutgoing(@NotNull SelectionKey selectionKey) throws IOException {
            ClientHandlingSuite clientHandlingSuite = (ClientHandlingSuite) selectionKey.attachment();
            switch (clientHandlingSuite.getStatus()) {
                case SENDING: {
                    MessageWriter messageWriter = clientHandlingSuite.getWriter();
                    if (messageWriter.write()) {
                        logger.info("Response sent to {}", ((SocketChannel) selectionKey.channel()).getRemoteAddress());
                        clientHandlingSuite.setStatus(ClientHandlingStatus.RECEIVING);
                        selectionKey.channel().register(selectionKey.selector(), SelectionKey.OP_READ);
                    }
                }
            }
        }

        /**
         * Does the necessary work with a channel ready for reading.
         * @param selectionKey the channel's selection key
         * @throws IOException if an I/O error occurs
         */
        private void handleIncoming(@NotNull SelectionKey selectionKey) throws IOException {
            ClientHandlingSuite clientHandlingSuite = (ClientHandlingSuite) selectionKey.attachment();
            MessageReader messageReader = clientHandlingSuite.getReader();
            Optional<Message> messageOptional = messageReader.read();
            if (messageOptional.isPresent()) {
                Message message = messageOptional.get();
                FtpQuery query = SerializationUtils.deserialize(message.getData());
                switch (query.getType()) {
                    case LIST: {
                        logger.info("List query received from {}",
                                ((SocketChannel) selectionKey.channel()).getRemoteAddress());
                        ArrayList<FtpListResponseItem> responseItems = new ArrayList<>();
                        Path path = Paths.get(query.getPath());
                        if (Files.exists(path)) {
                            File file = path.toRealPath().toFile();
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File item : files) {
                                    responseItems.add(new FtpListResponseItem(item.getName(), item.isDirectory()));
                                }
                            }
                        }
                        MessageWriter writer = clientHandlingSuite.getWriter();
                        writer.startNewMessage(
                                new Message(SerializationUtils.serialize(new FtpListResponse(responseItems)))
                        );
                        clientHandlingSuite.setStatus(ClientHandlingStatus.SENDING);
                        break;
                    }
                }
                selectionKey.channel().register(selectionKey.selector(), SelectionKey.OP_WRITE);
            }
        }


        /**
         * Does the necessary work with a channel ready for accepting.
         * @param selectionKey the channel's selection key
         * @throws IOException if an I/O error occurs
         */
        private void handleAccept(@NotNull SelectionKey selectionKey) throws IOException {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverChannel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                SelectionKey clientSelectionKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
                clientSelectionKey.attach(
                        new ClientHandlingSuite(
                                new MessageReader(socketChannel),
                                new MessageWriter(socketChannel)
                        )
                );
            }
        }
    }
}
