package ru.spbau.eshcherbin.hw4.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.eshcherbin.hw4.messages.MessageReader;
import ru.spbau.eshcherbin.hw4.messages.MessageWriter;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A helper class that contains all the information that should be attached to client's socket clientChannel client's socket clientChannel.
 */
class ClientHandlingSuite {
    private final @NotNull MessageReader reader;
    private final @NotNull MessageWriter writer;
    private @Nullable FileChannel fileChannel;
    private @NotNull ClientHandlingStatus status;

    public ClientHandlingSuite(@NotNull MessageReader reader,
                               @NotNull MessageWriter writer) {
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

    /**
     * Returns the file channel which content is to be transferred to the client if present.
     * @return the file channel
     */
    public @Nullable FileChannel getFileChannel() {
        return fileChannel;
    }

    /**
     * Sets a file channel which content is to be transferred to the client.
     * @param fileChannel the file channel
     */
    public void setFileChannel(@NotNull FileChannel fileChannel) {
        if (this.fileChannel != null && this.fileChannel.isOpen()) {
            try {
                this.fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.fileChannel = fileChannel;
    }
}
