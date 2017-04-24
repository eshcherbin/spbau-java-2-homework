package ru.spbau.eshcherbin.hw4.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Utility class that is used to write messages to a non-blocking channel.
 */
public class MessageWriter {
    private @NotNull ByteBuffer lengthBuffer = ByteBuffer.allocate(Message.LENGTH_BYTES);
    private @Nullable ByteBuffer dataBuffer;
    private @NotNull WritableByteChannel channel;

    /**
     * Constructs a message writer for a specific channel.
     * The channel should be in a non-blocking mode.
     * @param channel the channel to write the messages to
     */
    public MessageWriter(@NotNull WritableByteChannel channel) {
        this.channel = channel;
    }

    /**
     * Starts writing a new message to the channel.
     * The previous message is discarded if present.
     * @param message the message to be written
     * @return whether the message has been completely written
     * @throws IOException if an I/O Error occurs
     */
    public boolean write(@NotNull Message message) throws IOException {
        lengthBuffer.clear();
        lengthBuffer.putInt(message.data.length);
        lengthBuffer.flip();
        dataBuffer = ByteBuffer.allocate(message.data.length);
        dataBuffer.put(message.data);
        dataBuffer.flip();
        return write();
    }

    /**
     * Writes current message to the channel.
     * @return true if current message has been completely written or no message is currently being written, <tt>false</tt> otherwise
     * @throws IOException if an I/O Error occurs
     */
    public boolean write() throws IOException {
        if (dataBuffer == null) {
            return true;
        }
        channel.write(lengthBuffer);
        if (!lengthBuffer.hasRemaining()) {
            channel.write(dataBuffer);
            if (!dataBuffer.hasRemaining()) {
                dataBuffer = null;
                return true;
            }
        }
        return false;
    }
}
