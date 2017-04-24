package ru.spbau.eshcherbin.hw4.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

/**
 * Utility class that is used to read messages from a non-blocking channel.
 */
public class MessageReader {
    private int length;
    private @NotNull ByteBuffer lengthBuffer = ByteBuffer.allocate(Message.LENGTH_BYTES);
    private @Nullable ByteBuffer dataBuffer;
    private @NotNull ReadableByteChannel channel;

    /**
     * Constructs a message reader for a specific channel.
     * The channel should be in a non-blocking mode.
     * @param channel the channel to read the messages from
     */
    public MessageReader(@NotNull ReadableByteChannel channel) {
        dataBuffer = null;
        this.channel = channel;
    }

    /**
     * Starts or continues reading a message from the channel.
     * @return an empty optional if the message is not fully read yet or the newly read message otherwise
     * @throws IOException if an I/O error occurs
     */
    public @NotNull Optional<Message> read() throws IOException {
        if (lengthBuffer.hasRemaining()) {
            channel.read(lengthBuffer);
            if (lengthBuffer.hasRemaining()) {
                return Optional.empty();
            }
            lengthBuffer.flip();
            length = lengthBuffer.getInt();
            dataBuffer = ByteBuffer.allocate(length);
        }
        if (dataBuffer == null) { // should not happen
            return Optional.empty();
        }
        channel.read(dataBuffer);
        if (dataBuffer.hasRemaining()) {
            return Optional.empty();
        }
        Message result = new Message(length);
        dataBuffer.flip();
        dataBuffer.get(result.data);
        return Optional.of(result);
    }
}
