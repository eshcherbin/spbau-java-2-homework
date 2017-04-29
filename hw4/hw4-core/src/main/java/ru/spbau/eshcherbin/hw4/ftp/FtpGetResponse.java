package ru.spbau.eshcherbin.hw4.ftp;

/**
 * FTP get response.
 * During standard communication it should be followed with raw file content.
 */
public class FtpGetResponse implements FtpResponse {
    private final long fileSize;

    public FtpGetResponse(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Returns the size of the file.
     * @return the size of the file
     */
    public long getFileSize() {
        return fileSize;
    }
}
