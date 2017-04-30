package ru.spbau.eshcherbin.hw4;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * Class that contains some project-wide configuration constants.
 */
public class Config {
    /**
     * The address to which the server is bound.
     */
    @NotNull
    public static final InetSocketAddress serverBindingAddress = new InetSocketAddress(1117);
}
