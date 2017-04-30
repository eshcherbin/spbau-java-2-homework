package ru.spbau.eshcherbin.hw4.client;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.hw4.ftp.FtpGetResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpQuery;
import ru.spbau.eshcherbin.hw4.ftp.FtpQueryType;
import ru.spbau.eshcherbin.hw4.messages.Message;
import ru.spbau.eshcherbin.hw4.messages.MessageReader;
import ru.spbau.eshcherbin.hw4.messages.MessageWriter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * The FTP client.
 */
public class FtpClient implements Client {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(FtpClient.class);
    private @Nullable SocketChannel channel;

    /**
     * Connects the client to the server.
     * @param serverAddress the address of the server
     * @throws IOException if an I/O error occurs
     * @throws ClientAlreadyConnectedException if the client is already connected
     */
    @Override
    public void connect(@NotNull SocketAddress serverAddress)
            throws IOException, ClientAlreadyConnectedException {
        if (isConnected()) {
            throw new ClientAlreadyConnectedException();
        }
        channel = SocketChannel.open(serverAddress);
        logger.info("Connected to {}", serverAddress);
    }

    /**
     * Disconnects the client.
     * @throws IOException if an I/O error occurs
     * @throws ClientNotConnectedException if the client is not connected
     */
    @Override
    public void disconnect() throws IOException, ClientNotConnectedException {
        if (!isConnected()) {
            throw new ClientNotConnectedException();
        }
        SocketAddress remoteAddress = channel.getRemoteAddress();
        channel.close();
        logger.info("Disconnected from {}", remoteAddress);
    }

    /**
     * Returns whether the client is connected to a server.
     *
     * @return whether the client is connected to a server
     */
    @Override
    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    /**
     * Performs the list query.
     * @param path the path argument of the list query.
     * @return the response.
     * @throws IOException if an I/O error occurs
     * @throws ClientNotConnectedException if the client is not connected
     */
    public @NotNull FtpListResponse executeList(@NotNull String path)
            throws IOException, ClientNotConnectedException {
        if (!isConnected()) {
            throw new ClientNotConnectedException();
        }
        Message queryMessage = new Message(SerializationUtils.serialize(new FtpQuery(FtpQueryType.LIST, path)));
        MessageWriter writer = new MessageWriter(channel);
        writer.startNewMessage(queryMessage);
        try {
            writer.write();
        } catch (IOException e) {
            logger.error("Unable to send the list query");
            return FtpListResponse.emptyResponse();
        }
        MessageReader reader = new MessageReader(channel);
        Optional<Message> messageOptional = reader.read();
        if (!messageOptional.isPresent()) {
            logger.error("Unable to receive the response to list query");
            return FtpListResponse.emptyResponse();
        }
        Message responseMessage = messageOptional.get();
        return SerializationUtils.deserialize(responseMessage.getData());
    }

    /**
     * Performs the get query.
     * @param path the path to the file on the server
     * @param savePath the path where the file should be saved
     * @throws IOException if an I/O error occurs
     * @throws ClientNotConnectedException if the client is not connected
     */
    public void executeGet(@NotNull String path, @NotNull Path savePath)
            throws ClientNotConnectedException, IOException {
        if (!isConnected()) {
            throw new ClientNotConnectedException();
        }
        FileChannel fileChannel = FileChannel.open(savePath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        Message queryMessage = new Message(SerializationUtils.serialize(new FtpQuery(FtpQueryType.GET, path)));
        MessageWriter writer = new MessageWriter(channel);
        writer.startNewMessage(queryMessage);
        try {
            writer.write();
        } catch (IOException e) {
            logger.error("Unable to send the list query");
            return;
        }
        MessageReader reader = new MessageReader(channel);
        Optional<Message> messageOptional = reader.read();
        if (!messageOptional.isPresent()) {
            logger.error("Unable to receive the response to list query");
            return;
        }
        Message responseMessage = messageOptional.get();
        FtpGetResponse response = SerializationUtils.deserialize(responseMessage.getData());
        long bytesReceived = fileChannel.transferFrom(channel, 0, response.getFileSize());
        if (bytesReceived != response.getFileSize()) {
            logger.error("Unable to receive the whole file: only {} out of {} bytes were received",
                    bytesReceived, response.getFileSize());
        }
    }
}
