package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
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

    /**
     * Reads a commit from a file.
     * @param repository the operated repository
     * @param commitSha the commit's sha
     * @return the commit
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws IOException if an I/O error occurs
     */
    protected static @NotNull VcsCommit readCommit(@NotNull NucleusRepository repository, @NotNull String commitSha)
            throws RepositoryCorruptException, IOException {
        if (!repository.isValidSha(commitSha)) {
            throw new RepositoryCorruptException();
        }
        List<String> commitLines = Files.readAllLines(repository.getObject(commitSha));
        if (commitLines.size() < 4) {
            throw new RepositoryCorruptException();
        }
        String treeSha = commitLines.get(0);
        if (!repository.isValidSha(treeSha)) {
            throw new RepositoryCorruptException();
        }
        String author = commitLines.get(1);
        String timeInMillisecondsString = commitLines.get(2);
        long timeInMilliseconds;
        try {
             timeInMilliseconds = Long.parseLong(timeInMillisecondsString);
        } catch (NumberFormatException e) {
            throw new RepositoryCorruptException();
        }
        int messageStartLineIndex = 3;
        while (messageStartLineIndex < commitLines.size() &&
                !commitLines.get(messageStartLineIndex).startsWith(Constants.MESSAGE_COMMIT_PREFIX)) {
            ++messageStartLineIndex;
        }
        if (messageStartLineIndex == commitLines.size()) {
            throw new RepositoryCorruptException();
        }
        StringBuilder messageBuilder = new StringBuilder(commitLines.get(messageStartLineIndex)
                .substring(Constants.MESSAGE_COMMIT_PREFIX.length()));
        for (int i = messageStartLineIndex + 1; i < commitLines.size(); ++i) {
            messageBuilder.append('\n');
            messageBuilder.append(commitLines.get(i));
        }
        String message = messageBuilder.toString();
        VcsCommit commit = new VcsCommit(treeSha, message, author, timeInMilliseconds);
        Set<String> parentShaSet = commit.getParents();
        for (int i = 3; i < messageStartLineIndex; ++i) {
            String line = commitLines.get(i);
            if (!line.startsWith(Constants.PARENT_COMMIT_PREFIX) ||
                    !repository.isValidSha(line.substring(Constants.PARENT_COMMIT_PREFIX.length()))) {
                throw new RepositoryCorruptException();
            }
            parentShaSet.add(line.substring(Constants.PARENT_COMMIT_PREFIX.length()));
        }
        return commit;
    }

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
