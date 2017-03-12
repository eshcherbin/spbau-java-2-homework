package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NucleusManager {
    public static @NotNull NucleusRepository initRepository(@NotNull Path path)
            throws IOException, DirectoryExpectedException, RepositoryAlreadyInitializedException {
        NucleusRepository repository = NucleusRepository.createRepository(path);
        Files.createDirectory(repository.getObjectsDirectory());
        Files.createDirectory(repository.getReferencesDirectory());
        Files.createFile(repository.getIndexFile());
        Files.createFile(repository.getHeadFile());
        return repository;
    }

    public static void updateIndex(@NotNull Path path) throws RepositoryNotInitializedException {
        path = path.toAbsolutePath();
        NucleusRepository repository;
        try {
            repository = NucleusRepository.findRepository(path);
        } catch (DirectoryExpectedException e) {
            throw new RepositoryNotInitializedException();
        }
        if (Files.isDirectory(path)) {
            //TODO: iterate through all files and add them recursively
            return;
        }
        if (repository == null) {
            //TODO: throw an exception
        }
        //TODO: add file to index if needed
        throw new NotImplementedException();
    }

    public static void commitChanges(@NotNull Path path) {
        //TODO: implement commit
        throw new NotImplementedException();
    }

    public static void checkoutRevision(@NotNull Path path, @NotNull String name) {
        //TODO: implement checkout
        throw new NotImplementedException();
    }

    public static @Nullable LogMessage getLog(@NotNull Path path) {
        //TODO: implement log
        throw new NotImplementedException();
    }

    public static void mergeCommit(@NotNull Path path, @NotNull String name) {
        //TODO: implement merge
        throw new NotImplementedException();
    }
}
