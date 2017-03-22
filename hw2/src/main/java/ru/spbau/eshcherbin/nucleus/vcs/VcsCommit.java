package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A commit.
 */
public class VcsCommit extends VcsObject {
    /**
     * Sha of this commit's tree.
     */
    private @NotNull String treeSha;

    /**
     * Commit message.
     */
    private @NotNull String message;

    /**
     * Author of this commit.
     */
    private @NotNull String author;

    /**
     * Date of this commit in milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    private long dateTimeInMilliseconds;

    /**
     * A set of this commit's parent commits.
     */
    private @NotNull Set<String> parents;

    public VcsCommit(@NotNull String treeSha, @NotNull String message,
                     @NotNull String author, long dateTimeInMilliseconds) {
        this.treeSha = treeSha;
        type = VcsObjectType.COMMIT;
        this.message = message;
        this.author = author;
        this.dateTimeInMilliseconds = dateTimeInMilliseconds;
        parents = new HashSet<>();
    }

    /**
     * Returns sha of this commit's tree.
     * @return sha of this commit's tree
     */
    public @NotNull String getTreeSha() {
        return treeSha;
    }

    /**
     * Returns commit message.
     * @return commit message
     */
    public @NotNull String getMessage() {
        return message;
    }

    /**
     * Returns author of this commit.
     * @return author of this commit
     */
    public @NotNull String getAuthor() {
        return author;
    }

    /**
     * Returns date of this commit in milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return date of this commit in milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public long getTimeInMilliseconds() {
        return dateTimeInMilliseconds;
    }

    /**
     * Returns a set of this commit's parent commits.
     * @return a set of this commit's parent commits
     */
    public @NotNull Set<String> getParents() {
        return parents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String toString() {
        String parentsString = getParents().stream()
                .map(parent -> Constants.PARENT_COMMIT_PREFIX + parent)
                .collect(Collectors.joining("\n"));
        return getTreeSha() + '\n' + getAuthor() + '\n' + getTimeInMilliseconds() +
                (parentsString.isEmpty() ? "" : '\n' + parentsString) + '\n' +
                Constants.MESSAGE_COMMIT_PREFIX + getMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getContent() {
        return toString().getBytes();
    }
}
