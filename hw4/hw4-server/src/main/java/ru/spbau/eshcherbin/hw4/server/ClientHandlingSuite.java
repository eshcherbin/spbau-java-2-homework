package ru.spbau.eshcherbin.hw4.server;

import org.jetbrains.annotations.NotNull;
import ru.spbau.eshcherbin.hw4.messages.MessageReader;
import ru.spbau.eshcherbin.hw4.messages.MessageWriter;

/**
 * A helper class that contains all the information that should be attached to client's socket channelo client's socket channel.
 */
class ClientHandlingSuite {
    private final @NotNull MessageReader reader;
    private final @NotNull MessageWriter writer;
    private @NotNull ClientHandlingStatus status;

    public ClientHandlingSuite(@NotNull MessageReader reader, @NotNull MessageWriter writer) {
        this.reader = reader;
        this.writer = writer;
        status = ClientHandlingStatus.RECEIVING;
    }

    /**
     * Returns the corresponding message reader.
     * @return the corresponding message reader
     */
    public @NotNull MessageReader getReader() {
        return reader;
    }

    /**
     * Returns the corresponding message writer.
     * @return the corresponding message writer
     */
    public @NotNull MessageWriter getWriter() {
        return writer;
    }

    /**
     * Returns current handling status.
     * @return current handling status
     */
    public @NotNull ClientHandlingStatus getStatus() {
        return status;
    }

    /**
     * Sets new handling status.
     * @param status new handling status
     */
    public void setStatus(@NotNull ClientHandlingStatus status) {
        this.status = status;
    }
}
