package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NucleusManager {
    private static final int OBJECT_DIRECTORY_NAME_LENGTH = 2;

    public static @NotNull NucleusRepository initRepository(@NotNull Path path)
            throws IOException, DirectoryExpectedException, RepositoryAlreadyInitializedException {
        NucleusRepository repository = NucleusRepository.createRepository(path);
        Files.createDirectory(repository.getObjectsDirectory());
        Files.createDirectory(repository.getReferencesDirectory());
        Files.createFile(repository.getIndexFile());
        Files.createFile(repository.getHeadFile());
        return repository;
    }

    private static String addVCSObject(@NotNull NucleusRepository repository, @NotNull VCSObject object)
            throws IOException {
        Path objectDirectoryPath = repository.getObjectsDirectory()
                .resolve(object.getSha().substring(0, OBJECT_DIRECTORY_NAME_LENGTH));
        if (!Files.exists(objectDirectoryPath)) {
            Files.createDirectory(objectDirectoryPath);
        }
        Files.write(objectDirectoryPath.resolve(object.getSha().substring(OBJECT_DIRECTORY_NAME_LENGTH)),
                                                object.getContent());
        return object.getSha();
    }

    private static String addFile(@NotNull NucleusRepository repository, @NotNull Path filePath) throws IOException {
        return addVCSObject(repository, new VCSBlob(Files.readAllBytes(filePath), filePath.getFileName().toString()));
    }

    private static void updateIndex(@NotNull NucleusRepository repository, @NotNull Map<Path, String> addedFiles)
            throws IOException, IndexFileCorruptException {
        Map<Path, String> index = new HashMap<>();
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        if (!Files.exists(repository.getIndexFile())) {
            throw new IndexFileCorruptException();
        }
        for (String line : Files.readAllLines(repository.getIndexFile())) {
            String[] splitResult = onTabSplitter.splitToList(line).toArray(new String[1]);
            if (splitResult.length != 2) {
                throw new IndexFileCorruptException();
            }
            try {
                index.put(Paths.get(splitResult[0]), splitResult[1]);
            } catch (InvalidPathException e) {
                throw new IndexFileCorruptException();
            }
        }
        index.putAll(addedFiles);
        Files.write(repository.getIndexFile(), index.entrySet().stream()
                .map(entry -> entry.getKey().toString() + '\t' + entry.getValue())
                .sorted()
                .collect(Collectors.toList()));
    }

    public static void addToIndex(@NotNull Path path)
            throws RepositoryNotInitializedException, IOException, IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository;
        if (path.getParent() == null) {
            throw new RepositoryNotInitializedException();
        }
        try {
            repository = NucleusRepository.findRepository(path.getParent());
        } catch (DirectoryExpectedException e) {
            throw new RuntimeException("path.getParent() (\"" + path.getParent().toString() +
                    "\" should be a directory but is not");
        }
        if (repository == null) {
            throw new RepositoryNotInitializedException();
        }
        Map<Path, String> addedFiles = new HashMap<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getRepositoryDirectory())) {
                try {
                    addedFiles.put(repository.getRootDirectory().relativize(filePath), addFile(repository, filePath));
                } catch (IOException e) {
                    // some file was not added
                }
            }
        });
        updateIndex(repository, addedFiles);
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
