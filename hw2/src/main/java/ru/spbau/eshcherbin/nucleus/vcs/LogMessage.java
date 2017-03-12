package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
