package ru.spbau.eshcherbin.hw4.ftp;

import org.jetbrains.annotations.NotNull;

/**
 * FTP query.
 */
public class FtpQuery implements FtpMessage {
    private final @NotNull FtpQueryType type;
    private final @NotNull String path;

    public FtpQuery(@NotNull FtpQueryType type, @NotNull String path) {
        this.type = type;
        this.path = path;
    }

    /**
     * Returns this query's type.
     * @return this query's type
     */
    public @NotNull FtpQueryType getType() {
        return type;
    }

    /**
     * Returns the path in query.
     * @return the path in query
     */
    public @NotNull String getPath() {
        return path;
    }
}
