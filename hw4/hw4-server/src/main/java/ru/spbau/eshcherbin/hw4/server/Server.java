package ru.spbau.eshcherbin.hw4.server;

import java.io.IOException;

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
