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
     * Sending a list response.
     */
    SENDING_LIST,
    /**
     * Sending a get response.
     */
    SENDING_GET,
}
