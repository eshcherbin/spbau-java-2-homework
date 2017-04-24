package ru.spbau.eshcherbin.hw4.ftp;

import org.jetbrains.annotations.NotNull;

/**
 * An item of list response.
 */
public class FtpListResponseItem {
    private final @NotNull String name;
    private final boolean isDirectory;

    public FtpListResponseItem(@NotNull String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
