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
    private static String addVCSObject(@NotNull NucleusRepository repository, @NotNull VCSObject object)
            throws IOException {
        Path objectDirectoryPath = repository.getObjectsDirectory()
                .resolve(object.getSha().substring(0, Constants.OBJECT_DIRECTORY_NAME_LENGTH));
        if (!Files.exists(objectDirectoryPath)) {
            Files.createDirectory(objectDirectoryPath);
        }
        Files.write(objectDirectoryPath.resolve(object.getSha().substring(Constants.OBJECT_DIRECTORY_NAME_LENGTH)),
                                                object.getContent());
        return object.getSha();
    }

    private static String addFile(@NotNull NucleusRepository repository, @NotNull Path filePath) throws IOException {
        return addVCSObject(repository, new VCSBlob(Files.readAllBytes(filePath), filePath.getFileName().toString()));
    }

    private static @NotNull Map<Path, String> readIndexFile(@NotNull NucleusRepository repository)
            throws IndexFileCorruptException, IOException {
        Map<Path, String> index = new HashMap<>();
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        if (!Files.exists(repository.getIndexFile())) {
            throw new IndexFileCorruptException();
        }
        for (String line : Files.readAllLines(repository.getIndexFile())) {
            List<String> splitResult = onTabSplitter.splitToList(line);
            if (splitResult.size() != 2 || !repository.isValidSha(splitResult.get(1))) {
                throw new IndexFileCorruptException();
            }
            try {
                index.put(Paths.get(splitResult.get(0)), splitResult.get(1));
            } catch (InvalidPathException e) {
                throw new IndexFileCorruptException();
            }
        }
        return index;
    }

    private static void updateIndex(@NotNull NucleusRepository repository, @NotNull Map<Path, String> addedFiles,
                                    @NotNull Set<Path> removedFiles)
            throws IOException, IndexFileCorruptException {
        Map<Path, String> index = readIndexFile(repository);
        index.putAll(addedFiles);
        index.keySet().removeAll(removedFiles);
        Files.write(repository.getIndexFile(), index.entrySet().stream()
                .map(entry -> entry.getKey().toString() + '\t' + entry.getValue())
                .sorted()
                .collect(Collectors.toList()));
    }

    private static @NotNull VCSTree collectTreeFromIndex(@NotNull NucleusRepository repository)
            throws IndexFileCorruptException, IOException {
        Map<Path, String> index = readIndexFile(repository);
        Map<Path, VCSTree> pathToTree = new HashMap<>();
        pathToTree.put(Paths.get(""), new VCSTree(""));
        for (Path path : index.keySet()) {
            for (Path prefixPath : path) {
                Path parent = prefixPath.getParent();
                if (parent == null) {
                    parent = Paths.get("");
                }
                Path name = prefixPath.getFileName();
                if (prefixPath.equals(path)) {
                    pathToTree.get(parent).addChild(
                            new VCSObjectWithNameAndKnownSha(
                                    name.toString(),
                                    index.get(path),
                                    VCSObjectType.BLOB
                            )
                    );
                } else if (!pathToTree.containsKey(prefixPath)) {
                    pathToTree.put(prefixPath, new VCSTree(name.toString()));
                }
            }
        }
        return pathToTree.get(Paths.get(""));
    }

    private static @NotNull VCSCommit readCommit(@NotNull String headCommitSha) {
        //TODO: implement reading commit from file
        throw new NotImplementedException();
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
        NucleusRepository repository;
        try {
            repository = NucleusRepository.resolveRepository(path, true);
        } catch (DirectoryExpectedException e) {
            throw new RuntimeException("resolveRepository throws DirectoryExpectedException from addToIndex" +
                    ", this should not happen");
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
        updateIndex(repository, addedFiles, Collections.emptySet());
    }

    public static void removeFromIndex(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository;
        try {
            repository = NucleusRepository.resolveRepository(path, true);
        } catch (DirectoryExpectedException e) {
            throw new RuntimeException("resolveRepository throws DirectoryExpectedException from removeFromIndex" +
                    ", this should not happen");
        }
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
            throws IOException, RepositoryNotInitializedException, HeadFileCorruptException,
            DirectoryExpectedException, IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            String parentSha = null;
            Path reference = repository.getReferencesDirectory().resolve(currentBranch);
            if (Files.exists(reference)) {
                parentSha = Files.readAllLines(reference).get(0);
            }
            VCSTree tree = collectTreeFromIndex(repository);
            addVCSObject(repository, tree);
            VCSCommit commit = new VCSCommit(tree, message, System.getProperty(Constants.USER_NAME_PROPERTY),
                    System.currentTimeMillis());
            if (parentSha != null) {
                commit.getParents().add(parentSha);
            }
            addVCSObject(repository, commit);
            Files.write(repository.getHeadFile(), currentHead.getBytes());
            if (!Files.exists(reference)) {
                Files.createFile(reference);
            }
            Files.write(reference, commit.getSha().getBytes());
        } else {
            VCSTree tree = collectTreeFromIndex(repository);
            addVCSObject(repository, tree);
            VCSCommit commit = new VCSCommit(tree, message, System.getProperty(Constants.USER_NAME_PROPERTY),
                    System.currentTimeMillis());
            commit.getParents().add(currentHead);
            String commitSha = addVCSObject(repository, commit);
            Files.write(repository.getHeadFile(), commitSha.getBytes());
        }
    }

    public static void newBranch(@NotNull Path path, @NotNull String branchName)
            throws IOException, RepositoryNotInitializedException, DirectoryExpectedException,
            HeadFileCorruptException, BranchAlreadyExistsException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        Path newReference = repository.getReferencesDirectory().resolve(branchName);
        if (Files.exists(newReference)) {
            throw new BranchAlreadyExistsException();
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            Files.copy(repository.getReferencesDirectory().resolve(currentBranch), newReference);
        } else {
            Files.write(newReference, currentHead.getBytes());
        }
        Files.write(repository.getHeadFile(), (Constants.REFERENCE_HEAD_PREFIX + branchName).getBytes());
    }

    public static @Nullable LogMessage getLog(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, DirectoryExpectedException,
            HeadFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String currentHead = repository.getCurrentHead();
        String headCommitSha;
        if (currentHead.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            Path currentBranchReference = repository.getReferencesDirectory().resolve(currentBranch);
            if (!Files.exists(currentBranchReference)) {
                throw new HeadFileCorruptException();
            }
            headCommitSha = Files.readAllLines(currentBranchReference).get(0);
        } else {
            headCommitSha = currentHead;
        }
        VCSCommit headCommit = readCommit(headCommitSha);
        // breadth-first commit graph traversal
        Set<String> presentShas = new HashSet<>();
        presentShas.add(headCommitSha);
        Queue<VCSCommit> queue = new LinkedList<>();
        queue.add(headCommit);
        List<VCSCommit> commits = new ArrayList<>();
        while (!queue.isEmpty()) {
            VCSCommit commit = queue.remove();
            commits.add(commit);
            for (String parentSha : commit.getParents()) {
                if (!presentShas.contains(parentSha)) {
                    presentShas.add(parentSha);
                    queue.add(readCommit(parentSha));
                }
            }
        }
        commits.sort(Comparator.comparingLong(VCSCommit::getTimeInMilliseconds));
        LogMessage currentLogMessage = null;
        for (VCSCommit commit : commits) {
            currentLogMessage = new LogMessage(commit, currentLogMessage);
        }
        return currentLogMessage;
    }

    public static void checkoutRevision(@NotNull Path path, @NotNull String name) {
        //TODO: implement checkout
        throw new NotImplementedException();
    }

    public static void mergeCommit(@NotNull Path path, @NotNull String name) {
        //TODO: implement merge
        throw new NotImplementedException();
    }
}
