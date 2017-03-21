package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Date;

public class LogMessage {
    private @NotNull VCSCommit commit;
    private @Nullable LogMessage nextLogMessage;

    public LogMessage(@NotNull VCSCommit commit, @Nullable LogMessage nextLogMessage) {
        this.commit = commit;
        this.nextLogMessage = nextLogMessage;
    }

    public @Nullable LogMessage getNextLogMessage() {
        return nextLogMessage;
    }

    public @NotNull String getMessage() {
        return "commit: " + commit.getSha()
                + '\n' + "author: " + commit.getAuthor()
                + '\n' + "date: " + DateFormat.getDateTimeInstance().format(new Date(commit.getTimeInMilliseconds()))
                + '\n' + commit.getMessage();
    }
}
