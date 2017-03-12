package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NucleusManager {
    public static void initRepository(@NotNull Path path) throws IOException {
        path = path.toAbsolutePath();
        if (!Files.isDirectory(path)) {
            //TODO: throw an exception
        }
        NucleusRepository repository = NucleusRepository.findRepository(path);
        if (repository == null) {
            //TODO: throw an exception
        }
        Files.createDirectory(repository.getRepositoryDirectory());
        Files.createDirectory(repository.getObjectsDirectory());
        Files.createDirectory(repository.getReferencesDirectory());
        Files.createFile(repository.getIndexFile());
    }

    public static void updateIndex(@NotNull Path path) {
        path = path.toAbsolutePath();
        if (Files.isDirectory(path)) {
            //TODO: iterate through all files and add them recursively
            return;
        }
        NucleusRepository repository= NucleusRepository.findRepository(path);
        if (repository == null) {
            //TODO: throw an exception
        }
        //TODO: add file to index if needed
        throw new NotImplementedException();
    }
}
