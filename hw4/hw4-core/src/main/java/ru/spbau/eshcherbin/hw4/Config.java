package ru.spbau.eshcherbin.hw4;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Class that contains some project-wide configuration constants.
 */
public class Config {
    /**
     * The address to which the server is bound.
     */
    @NotNull
    public static final SocketAddress serverBindingAddress = new InetSocketAddress(1117);
}
