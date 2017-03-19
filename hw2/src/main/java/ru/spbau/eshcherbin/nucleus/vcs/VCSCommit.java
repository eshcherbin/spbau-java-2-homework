package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VCSCommit extends VCSObject {
    private @NotNull VCSTree tree;
    private @NotNull String message;
    private @NotNull String author;
    private long timeInMilliseconds;
    private @NotNull Set<String> parents;

    public VCSCommit(@NotNull VCSTree tree, @NotNull String message, @NotNull String author, long timeInMilliseconds) {
        this.tree = tree;
        type = VCSObjectType.COMMIT;
        this.message = message;
        this.author = author;
        this.timeInMilliseconds = timeInMilliseconds;
        parents = new HashSet<>();
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

    public @NotNull Set<String> getParents() {
        return parents;
    }

    private @NotNull String getStringContent() {
        String parentsString = getParents().stream()
                .map(parent -> Constants.PARENT_COMMIT_PREFIX + parent)
                .collect(Collectors.joining("\n"));
        return getTree().getSha() + '\n' + getAuthor() + '\n' + getTimeInMilliseconds() +
                (parentsString.isEmpty() ? "" : '\n' + parentsString) + '\n' + getMessage();
    }

    @Override
    public byte[] getContent() {
        return getStringContent().getBytes();
    }
}
