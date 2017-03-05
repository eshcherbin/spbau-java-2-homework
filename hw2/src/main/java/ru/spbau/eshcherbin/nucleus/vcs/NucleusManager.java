package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NucleusManager {
    private static @Nullable Path findRepositoryDirectory(@NotNull Path path) {
        //TODO: implement going through all predecessors and searching for a '.nuc' directory
        return null;
    }

    public static void initRepository(@NotNull Path path) throws IOException {
        path = path.toAbsolutePath();
        if (!Files.isDirectory(path)) {
            //TODO: throw an exception
        }
        if (findRepositoryDirectory(path) != null) {
            //TODO: throw an exception
        }
        Path repositoryDirectory = path.resolve(Constants.REPOSITORY_DIRECTORY_NAME);
        Files.createDirectory(repositoryDirectory);
        Files.createDirectory(repositoryDirectory.resolve(Constants.OBJECTS_DIRECTORY_NAME));
        Files.createDirectory(repositoryDirectory.resolve(Constants.REFERENCES_DIRECTORY_NAME));
        Files.createFile(repositoryDirectory.resolve(Constants.INDEX_FILE_NAME));
    }

    public static void updateIndex(@NotNull Path path) {
        path = path.toAbsolutePath();
        if (Files.isDirectory(path)) {
            //TODO: iterate through all files and add them recursively
            return;
        }
        Path repositoryDirectory = findRepositoryDirectory(path);
        if (repositoryDirectory == null) {
            //TODO: throw an exception
        }
        //TODO: add file to index if needed
        throw new NotImplementedException();
    }

    //TODO: implement a Repository class to hide the mechanics of getting objects or references directory
}
