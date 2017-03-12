package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class NucleusRepository {
    private @NotNull Path repositoryDirectory;

    private NucleusRepository(@NotNull Path repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    public static @Nullable NucleusRepository findRepository(@NotNull Path path) throws DirectoryExpectedException, IOException {
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
