package ru.spbau.eshcherbin.hw4.messages;

import org.jetbrains.annotations.NotNull;

/**
 * A unit of server-client communication.
 */
public class Message {
    /**
     * Size of the length block in bytes.
     */
    public static final int LENGTH_BYTES = 4;

    /**
     * Content of the message.
     */
    protected final @NotNull byte[] data;

    /**
     * Creates an empty message of given length.
     * @param length length of the message
     */
    public Message(int length) {
        data = new byte[length];
    }

    /**
     * Creates a message with given content.
     * @param data the content of the message
     */
    public Message(@NotNull byte[] data) {
        this.data = data;
    }

    /**
     * Returns the content of the message.
     * @return the content of the message
     */
    public @NotNull byte[] getData() {
        return data;
    }
}
