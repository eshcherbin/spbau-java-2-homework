package ru.spbau.eshcherbin.hw4.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Interface of a server.
 */
public interface Client {
    /**
     * Connects the client to the server.
     * @param serverAddress the address of the server
     * @throws IOException if an I/O error occurs
     */
    void connect(@NotNull SocketAddress serverAddress) throws IOException, ClientAlreadyConnectedException;

    /**
     * Disconnects the client.
     * @throws IOException if an I/O error occurs
     */
    void disconnect() throws IOException, ClientNotConnectedException;

    /**
     * Returns whether the client is connected to a server.
     * @return whether the client is connected to a server
     */
    boolean isConnected();
}
