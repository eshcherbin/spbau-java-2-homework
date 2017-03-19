package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

public class NucleusRepository {
    private @NotNull Path repositoryDirectory;

    private NucleusRepository(@NotNull Path repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    public static @Nullable NucleusRepository findRepository(@NotNull Path path)
            throws DirectoryExpectedException, IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (!Files.isDirectory(path)) {
            throw new DirectoryExpectedException();
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

    public static @NotNull NucleusRepository createRepository(@NotNull Path path)
            throws RepositoryAlreadyInitializedException, DirectoryExpectedException, IOException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        if (findRepository(path) != null) {
            throw new RepositoryAlreadyInitializedException();
        }

        Path repositoryDirectory = path.resolve(Constants.REPOSITORY_DIRECTORY_NAME);
        Files.createDirectory(repositoryDirectory);
        return new NucleusRepository(repositoryDirectory);
    }

    //TODO: add test
    public static @NotNull NucleusRepository resolveRepository(@NotNull Path path, boolean startFromParent)
            throws RepositoryNotInitializedException, IOException, DirectoryExpectedException {
        NucleusRepository repository;
        if (path.getParent() == null) {
            throw new RepositoryNotInitializedException();
        }
        try {
            repository = findRepository(startFromParent ? path.getParent() : path);
        } catch (DirectoryExpectedException e) {
            if (startFromParent) {
                throw new RuntimeException("path.getParent() (\"" + path.getParent().toString() +
                        "\" should be a directory but is not");
            } else {
                throw e;
            }
        }
        if (repository == null) {
            throw new RepositoryNotInitializedException();
        }
        return repository;
    }

    public @NotNull Path getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public @NotNull Path getRootDirectory() {
        return repositoryDirectory.getParent();
    }

    public @NotNull Path getObjectsDirectory() {
        return repositoryDirectory.resolve(Constants.OBJECTS_DIRECTORY_NAME);
    }

    public @NotNull Path getReferencesDirectory() {
        return repositoryDirectory.resolve(Constants.REFERENCES_DIRECTORY_NAME);
    }

    public @NotNull Path getIndexFile() {
        return repositoryDirectory.resolve(Constants.INDEX_FILE_NAME);
    }

    public @NotNull Path getHeadFile() {
        return repositoryDirectory.resolve(Constants.HEAD_FILE_NAME);
    }

    //TODO: add test
    public @NotNull String getCurrentHead() throws IOException, HeadFileCorruptException {
        if (!Files.exists(getHeadFile())) {
            throw new HeadFileCorruptException();
        }
        List<String> headLines = Files.readAllLines(getHeadFile());
        if (headLines.size() == 0) {
            return Constants.DEFAULT_BRANCH_NAME;
        } else if (headLines.size() > 1) {
            throw new HeadFileCorruptException();
        } else {
            String head = headLines.get(0);
            if (!Files.exists(getReferencesDirectory().resolve(head))) {
                throw new HeadFileCorruptException();
            }
            return head;
        }
    }

    //TODO: add test
    public boolean isValidSha(@NotNull String sha) {
        return sha.length() > Constants.OBJECT_DIRECTORY_NAME_LENGTH && Files.exists(getObject(sha));
    }

    //TODO: add test
    //TODO: rewrite code using this method where needed
    public Path getObject(@NotNull String sha) {
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
