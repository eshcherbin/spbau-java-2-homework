package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VcsCommit extends VcsObject {
    private @NotNull String treeSha;
    private @NotNull String message;
    private @NotNull String author;
    private long timeInMilliseconds;
    private @NotNull Set<String> parents;

    public VcsCommit(@NotNull String treeSha, @NotNull String message,
                     @NotNull String author, long timeInMilliseconds) {
        this.treeSha = treeSha;
        type = VcsObjectType.COMMIT;
        this.message = message;
        this.author = author;
        this.timeInMilliseconds = timeInMilliseconds;
        parents = new HashSet<>();
    }

    public @NotNull String getTreeSha() {
        return treeSha;
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
        return getTreeSha() + '\n' + getAuthor() + '\n' + getTimeInMilliseconds() +
                (parentsString.isEmpty() ? "" : '\n' + parentsString) + '\n' +
                Constants.MESSAGE_COMMIT_PREFIX + getMessage();
    }

    @Override
    public byte[] getContent() {
        return getStringContent().getBytes();
    }
}
