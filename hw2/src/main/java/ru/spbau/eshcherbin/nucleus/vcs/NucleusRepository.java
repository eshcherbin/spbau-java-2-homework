package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class NucleusRepository {
    private @NotNull Path repositoryDirectory;

    public NucleusRepository(@NotNull Path repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory.toAbsolutePath();
        //TODO: check repositoryDirectory
    }

    public static @Nullable NucleusRepository findRepository(@NotNull Path path) {
        //TODO: implement going through all predecessors and searching for a '.nuc' directory
        return null;
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
}
