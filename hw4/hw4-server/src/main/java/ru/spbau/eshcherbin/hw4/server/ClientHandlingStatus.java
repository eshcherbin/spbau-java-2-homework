package ru.spbau.eshcherbin.hw4.server;

/**
 * Current status of the client.
 */
enum ClientHandlingStatus {
    /**
     * Receiving a query.
     */
    RECEIVING,
    /**
     * Sending a response.
     */
    SENDING,
}
