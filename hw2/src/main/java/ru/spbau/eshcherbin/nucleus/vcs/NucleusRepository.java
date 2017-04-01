package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

/**
 * A representation of a particular repository that is used for convenient access to its inner structure.
 */
public class NucleusRepository {
    /**
     * The path to the repository directory.
     */
    private @NotNull Path repositoryDirectory;

    private NucleusRepository(@NotNull Path repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    /**
     * Searches for a repository that contains given path.
     * @param path the path to start the search from
     * @return the repository if found, <tt>null</tt> otherwise
     * @throws IOException if an I/O error occurs
     */
    public static @Nullable NucleusRepository findRepository(@NotNull Path path)
            throws IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!Files.isDirectory(path)) {
            path = path.getParent();
        }
        while (path != null) {
            Path repositoryDirectory = path.resolve(Constants.REPOSITORY_DIRECTORY_NAME);
            if (Files.exists(repositoryDirectory)) {
                return new NucleusRepository(repositoryDirectory);
            }
            path = path.getParent();
        }
        return null;
    }

    /**
     * Checks that a repository can be created at a given location and creates a repository folder there.
     * Its inner structure is not initialized, use {@link NucleusManager#initializeRepository(Path)} instead.
     * @param path the location where a repository is to be created
     * @return the repository
     * @throws IOException if an I/O error occurs
     * @throws DirectoryExpectedException if the path does not refer to a directory
     * @throws RepositoryAlreadyInitializedException if the path refers to a location inside an already existing
     *                                               repository
     */
    public static @NotNull NucleusRepository createRepository(@NotNull Path path)
            throws RepositoryAlreadyInitializedException, DirectoryExpectedException, IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!Files.isDirectory(path)) {
            logger.error(fatalMarker, "No index file found");
            throw new DirectoryExpectedException();
        }
        if (findRepository(path) != null) {
            throw new RepositoryAlreadyInitializedException();
        }
        Path repositoryDirectory = path.resolve(Constants.REPOSITORY_DIRECTORY_NAME);
        Files.createDirectory(repositoryDirectory);
        return new NucleusRepository(repositoryDirectory);
    }

    /**
     * Searches for a repository that contains given path. Similar to {@link NucleusRepository#findRepository(Path)} but
     * throws an exception when fails.
     * @param path the path to start the search from
     * @param startFromParent whether the search should start from <tt>path</tt>'s parent
     * @return the repository
     * @throws RepositoryNotInitializedException if no repository is found
     * @throws IOException if an I/O error occurs
     */
    public static @NotNull NucleusRepository resolveRepository(@NotNull Path path, boolean startFromParent)
            throws RepositoryNotInitializedException, IOException {
        NucleusRepository repository;
        if (path.getParent() == null) {
            throw new RepositoryNotInitializedException();
        }
        repository = findRepository(startFromParent ? path.getParent() : path);
        if (repository == null) {
            throw new RepositoryNotInitializedException();
        }
        return repository;
    }

    /**
     * Returns the path to the repository's inner directory.
     * @return the path to the repository's inner directory
     */
    public @NotNull Path getRepositoryDirectory() {
        return repositoryDirectory;
    }

    /**
     * Returns the path to the repository's root directory.
     * @return the path to the repository's root directory
     */
    public @NotNull Path getRootDirectory() {
        return repositoryDirectory.getParent();
    }

    /**
     * Returns the path to the repository's objects directory.
     * @return the path to the repository's objects directory
     */
    public @NotNull Path getObjectsDirectory() {
        return repositoryDirectory.resolve(Constants.OBJECTS_DIRECTORY_NAME);
    }

    /**
     * Returns the path to the repository's references directory.
     * @return the path to the repository's references directory
     */
    public @NotNull Path getReferencesDirectory() {
        return repositoryDirectory.resolve(Constants.REFERENCES_DIRECTORY_NAME);
    }

    /**
     * Returns the path to the repository's index file.
     * @return the path to the repository's index file
     */
    public @NotNull Path getIndexFile() {
        return repositoryDirectory.resolve(Constants.INDEX_FILE_NAME);
    }

    /**
     * Returns the path to the repository's HEAD file.
     * @return the path to the repository's HEAD file
     */
    public @NotNull Path getHeadFile() {
        return repositoryDirectory.resolve(Constants.HEAD_FILE_NAME);
    }

    /**
     * Returns the current HEAD file's content
     * @return the current HEAD file's content
     * @throws IOException if an I/O error occurs
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     */
    public @NotNull String getCurrentHead() throws IOException, HeadFileCorruptException {
        if (!Files.exists(getHeadFile())) {
            throw new HeadFileCorruptException();
        }
        List<String> headLines = Files.readAllLines(getHeadFile());
        if (headLines.size() == 0) {
            return Constants.REFERENCE_HEAD_PREFIX + Constants.DEFAULT_BRANCH_NAME;
        } else if (headLines.size() > 1) {
            throw new HeadFileCorruptException();
        } else {
            String head = headLines.get(0);
            if (head.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
                String branchName = head.substring(Constants.REFERENCE_HEAD_PREFIX.length());
                if (!Files.exists(getReferencesDirectory().resolve(branchName))) {
                    throw new HeadFileCorruptException();
                }
            } else {
                if (!isValidSha(head)) {
                    throw new HeadFileCorruptException();
                }
            }
            return head;
        }
    }

    /**
     * Checks if the given string is a valid sha of some object within this repository
     * @param shaToCheck the sha that is checked
     * @return whether the given string is a valid sha of some object within this repository
     */
    public boolean isValidSha(@NotNull String shaToCheck) {
        return shaToCheck.length() > Constants.OBJECT_DIRECTORY_NAME_LENGTH && Files.exists(getObject(shaToCheck));
    }

    /**
     * Returns the path to the object with the given sha. Be careful! The sha is not checked for validity, use
     * {@link NucleusRepository#isValidSha(String)} first.
     * @param sha the sha of the object
     * @return the path to the object with the given sha
     */
    public @NotNull Path getObject(@NotNull String sha) {
        Path subDirectory =
                getObjectsDirectory().resolve(sha.substring(0, Constants.OBJECT_DIRECTORY_NAME_LENGTH));
        return subDirectory.resolve(sha.substring(Constants.OBJECT_DIRECTORY_NAME_LENGTH));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NucleusRepository that = (NucleusRepository) o;

        return repositoryDirectory.equals(that.repositoryDirectory);
    }

    @Override
    public int hashCode() {
        return repositoryDirectory.hashCode();
    }
}
