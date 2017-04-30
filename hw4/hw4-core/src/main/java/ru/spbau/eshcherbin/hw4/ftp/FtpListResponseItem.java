package ru.spbau.eshcherbin.hw4.ftp;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * An item of list response.
 */
public class FtpListResponseItem implements Serializable {
    private final @NotNull String fileName;
    private final boolean isDirectory;

    public FtpListResponseItem(@NotNull String fileName, boolean isDirectory) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    /**
     * Returns the name of the file.
     * @return the name of the file
     */
    public @NotNull String getFileName() {
        return fileName;
    }

    /**
     * Returns whether the file is a directory.
     * @return whether the file is a directory
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FtpListResponseItem that = (FtpListResponseItem) o;
        return isDirectory == that.isDirectory && fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + (isDirectory ? 1 : 0);
        return result;
    }
}
