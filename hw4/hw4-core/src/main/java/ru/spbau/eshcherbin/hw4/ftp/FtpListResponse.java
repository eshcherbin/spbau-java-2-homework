package ru.spbau.eshcherbin.hw4.ftp;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FtpListResponse implements FtpResponse {
    private final @NotNull ArrayList<FtpListResponseItem> responseItems;

    public FtpListResponse(@NotNull ArrayList<FtpListResponseItem> responseItems) {
        this.responseItems = responseItems;
    }

    public @NotNull ArrayList<FtpListResponseItem> getResponseItems() {
        return responseItems;
    }
}
