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

    public Message(int length) {
        data = new byte[length];
    }

    public @NotNull byte[] getData() {
        return data;
    }
}
