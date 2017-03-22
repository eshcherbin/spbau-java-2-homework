package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Date;

/**
 * A log message about one commit.
 */
public class LogMessage {
    /**
     * The commit this log message provides information about.
     */
    private @NotNull VcsCommit commit;

    /**
     * Next log message if there is any.
     */
    private @Nullable LogMessage nextLogMessage;

    public LogMessage(@NotNull VcsCommit commit, @Nullable LogMessage nextLogMessage) {
        this.commit = commit;
        this.nextLogMessage = nextLogMessage;
    }

    /**
     * Returns the following log message.
     * @return the next log message
     */
    public @Nullable LogMessage getNextLogMessage() {
        return nextLogMessage;
    }

    /**
     * Returns the information string about the commit.
     * @return the information string about the commit
     */
    public @NotNull String getMessage() {
        return "commit: " + commit.getSha()
                + '\n' + "author: " + commit.getAuthor()
                + '\n' + "date: " + DateFormat.getDateTimeInstance().format(new Date(commit.getTimeInMilliseconds()))
                + '\n' + commit.getMessage();
    }
}
