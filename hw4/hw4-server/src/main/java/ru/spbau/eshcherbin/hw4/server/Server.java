package ru.spbau.eshcherbin.hw4.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Interface of a server.
 */
public interface Server {
    /**
     * Starts the server.
     */
    void start() throws IOException;

    /**
     * Stops the server.
     */
    void stop();
}
