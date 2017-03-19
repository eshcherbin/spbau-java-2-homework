package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public class VCSCommit extends VCSObject {
    private @NotNull VCSTree tree;
    private @NotNull String message;
    private @NotNull String author;
    private long timeInMilliseconds;

    public VCSCommit(@NotNull VCSTree tree, @NotNull String message, @NotNull String author, long timeInMilliseconds) {
        this.tree = tree;
        type = VCSObjectType.COMMIT;
        this.message = message;
        this.author = author;
        this.timeInMilliseconds = timeInMilliseconds;
    }

    public @NotNull VCSTree getTree() {
        return tree;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public @NotNull String getAuthor() {
        return author;
    }

    public long getTimeInMilliseconds() {
        return timeInMilliseconds;
    }

    private @NotNull String getStringContent() {
        return getTree().getSha() + '\n' + getAuthor() + '\n' + getTimeInMilliseconds() + '\n' + getMessage();
    }

    @Override
    public byte[] getContent() {
        return getStringContent().getBytes();
    }
}
