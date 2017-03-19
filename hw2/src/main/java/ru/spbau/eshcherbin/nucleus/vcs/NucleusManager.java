package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class NucleusManager {
    private static final int OBJECT_DIRECTORY_NAME_LENGTH = 2;
    private static final String USER_NAME_PROPERTY = "user.name";

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

    private static void updateIndex(@NotNull NucleusRepository repository, @NotNull Map<Path, String> addedFiles,
                                    @NotNull Set<Path> removedFiles)
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
        index.keySet().removeAll(removedFiles);
        Files.write(repository.getIndexFile(), index.entrySet().stream()
                .map(entry -> entry.getKey().toString() + '\t' + entry.getValue())
                .sorted()
                .collect(Collectors.toList()));
    }

    private static @NotNull VCSTree collectTree(@NotNull NucleusRepository repository) {
        return new VCSTree("");
    }

    public static @NotNull NucleusRepository initRepository(@NotNull Path path)
            throws IOException, DirectoryExpectedException, RepositoryAlreadyInitializedException {
        NucleusRepository repository = NucleusRepository.createRepository(path);
        Files.createDirectory(repository.getObjectsDirectory());
        Files.createDirectory(repository.getReferencesDirectory());
        Files.createFile(repository.getIndexFile());
        Files.createFile(repository.getHeadFile());
        return repository;
    }

    public static void addToIndex(@NotNull Path path)
            throws RepositoryNotInitializedException, IOException, IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path);
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
        updateIndex(repository, addedFiles, Collections.emptySet());
    }

    public static void removeFromIndex(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path);
        Set<Path> removedFiles = new HashSet<>();
        Files.walk(path).forEach(filePath -> {
            filePath = filePath.toAbsolutePath().normalize();
            if (Files.isRegularFile(filePath) && !filePath.startsWith(repository.getRepositoryDirectory())) {
                removedFiles.add(repository.getRootDirectory().relativize(filePath));
            }
        });
        Files.delete(path);
        updateIndex(repository, Collections.emptyMap(), removedFiles);
    }

    public static void commitChanges(@NotNull Path path, @NotNull String message)
            throws IOException, RepositoryNotInitializedException, HeadFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path);
        VCSTree tree = collectTree(repository);
        VCSCommit commit = new VCSCommit(tree, message, System.getProperty(USER_NAME_PROPERTY),
                                         System.currentTimeMillis());
        addVCSObject(repository, commit);
        String currentBranch = repository.getCurrentHead();
        Files.write(repository.getHeadFile(), currentBranch.getBytes());
        Path reference = repository.getReferencesDirectory().resolve(currentBranch);
        if (!Files.exists(reference)) {
            Files.createFile(reference);
        }
        Files.write(reference, commit.getSha().getBytes());
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
