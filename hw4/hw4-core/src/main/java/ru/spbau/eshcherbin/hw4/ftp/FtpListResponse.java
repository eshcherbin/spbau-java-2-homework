package ru.spbau.eshcherbin.hw4.ftp;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * FTP list response.
 */
public class FtpListResponse implements FtpResponse {
    private final @NotNull ArrayList<FtpListResponseItem> responseItems;

    public FtpListResponse(@NotNull ArrayList<FtpListResponseItem> responseItems) {
        this.responseItems = responseItems;
    }

    /**
     * Returns items of the response.
     * @return items of the response
     */
    public @NotNull ArrayList<FtpListResponseItem> getResponseItems() {
        return responseItems;
    }

    /**
     * Returns an empty response.
     * @return an empty response
     */
    public static @NotNull FtpListResponse emptyResponse() {
        return new FtpListResponse(new ArrayList<>());
    }
}
