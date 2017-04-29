package ru.spbau.eshcherbin.hw4.client;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpQuery;
import ru.spbau.eshcherbin.hw4.ftp.FtpQueryType;
import ru.spbau.eshcherbin.hw4.messages.Message;
import ru.spbau.eshcherbin.hw4.messages.MessageReader;
import ru.spbau.eshcherbin.hw4.messages.MessageWriter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
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
     */
    @Override
    public void connect(@NotNull SocketAddress serverAddress)
            throws IOException, ClientAlreadyConnectedException {
        if (channel != null && channel.isConnected()) {
            throw new ClientAlreadyConnectedException();
        }
        channel = SocketChannel.open(serverAddress);
        logger.info("Connected to {}", serverAddress);
    }

    /**
     * Disconnects the client.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void disconnect() throws IOException, ClientNotConnectedException {
        if (channel == null || !channel.isConnected()) {
            throw new ClientNotConnectedException();
        }
        SocketAddress remoteAddress = channel.getRemoteAddress();
        channel.close();
        logger.info("Disconnected from {}", remoteAddress);
    }

    /**
     * Performs the list query.
     * @param path the path argument of the list query.
     * @return the response.
     * @throws IOException if an I/O error occurs
     */
    public @NotNull FtpListResponse executeList(@NotNull String path)
            throws IOException, ClientNotConnectedException {
        if (channel == null || !channel.isConnected()) {
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

    //TODO: implement get
}
