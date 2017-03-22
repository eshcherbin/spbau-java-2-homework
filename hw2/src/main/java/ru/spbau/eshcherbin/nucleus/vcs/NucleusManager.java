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
        index.keySet().removeAll(removedFiles);
        index.putAll(addedFiles);
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

    private static @NotNull VCSCommit readCommit(@NotNull NucleusRepository repository, @NotNull String commitSha)
            throws RepositoryCorruptException, IOException {
        if (!repository.isValidSha(commitSha)) {
            throw new RepositoryCorruptException();
        }
        List<String> commitLines = Files.readAllLines(repository.getObject(commitSha));
        if (commitLines.size() < 4) {
            throw new RepositoryCorruptException();
        }
        String treeSha = commitLines.get(0);
        if (!repository.isValidSha(treeSha)) {
            throw new RepositoryCorruptException();
        }
        String author = commitLines.get(1);
        String timeInMillisecondsString = commitLines.get(2);
        long timeInMilliseconds;
        try {
             timeInMilliseconds = Long.parseLong(timeInMillisecondsString);
        } catch (NumberFormatException e) {
            throw new RepositoryCorruptException();
        }
        int messageStartLineIndex = 3;
        while (messageStartLineIndex < commitLines.size() &&
                !commitLines.get(messageStartLineIndex).startsWith(Constants.MESSAGE_COMMIT_PREFIX)) {
            ++messageStartLineIndex;
        }
        if (messageStartLineIndex == commitLines.size()) {
            throw new RepositoryCorruptException();
        }
        StringBuilder messageBuilder = new StringBuilder(commitLines.get(messageStartLineIndex)
                .substring(Constants.MESSAGE_COMMIT_PREFIX.length()));
        for (int i = messageStartLineIndex + 1; i < commitLines.size(); ++i) {
            messageBuilder.append('\n');
            messageBuilder.append(commitLines.get(i));
        }
        String message = messageBuilder.toString();
        VCSCommit commit = new VCSCommit(treeSha, message, author, timeInMilliseconds);
        Set<String> parentShaSet = commit.getParents();
        for (int i = 3; i < messageStartLineIndex; ++i) {
            String line = commitLines.get(i);
            if (!line.startsWith(Constants.PARENT_COMMIT_PREFIX) ||
                    !repository.isValidSha(line.substring(Constants.PARENT_COMMIT_PREFIX.length()))) {
                throw new RepositoryCorruptException();
            }
            parentShaSet.add(line.substring(Constants.PARENT_COMMIT_PREFIX.length()));
        }
        return commit;
    }

    private static @NotNull VCSTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha)
            throws RepositoryCorruptException, IOException {
        return readTree(repository, treeSha, "");
    }

    private static @NotNull VCSTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha,
                                             @NotNull String treeName)
            throws RepositoryCorruptException, IOException {
        if (!repository.isValidSha(treeSha)) {
            throw new RepositoryCorruptException();
        }
        VCSTree tree = new VCSTree(treeName);
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        for (String line : Files.readAllLines(repository.getObject(treeSha))) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            if (splitResults.size() != 3) {
                throw new RepositoryCorruptException();
            }
            VCSObjectType type;
            try {
                type = VCSObjectType.valueOf(splitResults.get(0));
            } catch (IllegalArgumentException e) {
                throw new RepositoryCorruptException();
            }
            if (!(type == VCSObjectType.BLOB || type == VCSObjectType.TREE)) {
                throw new RepositoryCorruptException();
            }
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            if (type == VCSObjectType.BLOB) {
                if (!repository.isValidSha(sha)) {
                    throw new RepositoryCorruptException();
                }
                tree.addChild(new VCSObjectWithNameAndKnownSha(name, sha, type));
            } else {
                tree.addChild(readTree(repository, sha, treeName));
            }
        }
        return tree;
    }

    private static @NotNull Map<Path, String> walkVcsTree(@NotNull VCSTree tree) {
        Map<Path, String> files = new HashMap<>();
        walkVcsTree(tree, Paths.get(""), files);
        return files;
    }

    private static void walkVcsTree(@NotNull VCSTree tree, @NotNull Path path, @NotNull Map<Path, String> filesMap) {
        for (VCSObjectWithName object : tree.children) {
            if (object.getType() == VCSObjectType.BLOB) {
                filesMap.put(path.resolve(object.getName()), object.getSha());
            } else {
                walkVcsTree((VCSTree) object, path.resolve(object.getName()), filesMap);
            }
        }
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
            VCSCommit commit = new VCSCommit(tree.getSha(), message, System.getProperty(Constants.USER_NAME_PROPERTY),
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
            VCSCommit commit = new VCSCommit(tree.getSha(), message, System.getProperty(Constants.USER_NAME_PROPERTY),
                    System.currentTimeMillis());
            commit.getParents().add(currentHead);
            String commitSha = addVCSObject(repository, commit);
            Files.write(repository.getHeadFile(), commitSha.getBytes());
        }
    }

    public static void createBranch(@NotNull Path path, @NotNull String branchName)
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

    public static @Nullable LogMessage getLog(@NotNull Path path, @NotNull String revisionName)
            throws IOException, RepositoryNotInitializedException, DirectoryExpectedException,
            RepositoryCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String startCommitSha;
        if (revisionName.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String branchName = revisionName.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            Path branchReference = repository.getReferencesDirectory().resolve(branchName);
            if (!Files.exists(branchReference)) {
                throw new RepositoryCorruptException();
            }
            startCommitSha = Files.readAllLines(branchReference).get(0);
        } else {
            startCommitSha = revisionName;
        }
        VCSCommit headCommit = readCommit(repository, startCommitSha);
        // breadth-first commit graph traversal
        Set<String> presentShaSet = new HashSet<>();
        presentShaSet.add(startCommitSha);
        Queue<VCSCommit> queue = new LinkedList<>();
        queue.add(headCommit);
        List<VCSCommit> commits = new ArrayList<>();
        while (!queue.isEmpty()) {
            VCSCommit commit = queue.remove();
            commits.add(commit);
            for (String parentSha : commit.getParents()) {
                if (!presentShaSet.contains(parentSha)) {
                    presentShaSet.add(parentSha);
                    queue.add(readCommit(repository, parentSha));
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

    public static @Nullable LogMessage getLog(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, DirectoryExpectedException,
            HeadFileCorruptException, RepositoryCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String currentHead = repository.getCurrentHead();
        return getLog(path, currentHead);
    }

    public static void checkoutRevision(@NotNull Path path, @NotNull String name)
            throws IOException, RepositoryNotInitializedException, DirectoryExpectedException,
            HeadFileCorruptException, RepositoryCorruptException, NoSuchRevisionException,
            IndexFileCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String newRevisionSha;
        Path possibleReference = repository.getReferencesDirectory().resolve(name);
        if (Files.exists(possibleReference)) {
            newRevisionSha = Files.readAllLines(possibleReference).get(0);
            if (!repository.isValidSha(newRevisionSha)) {
                throw new RepositoryCorruptException();
            }
        } else {
            newRevisionSha = name;
            if (!repository.isValidSha(newRevisionSha)) {
                throw new NoSuchRevisionException();
            }
        }
        String currentHead = repository.getCurrentHead();
        String currentRevisionSha;
        if (currentHead.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            Path reference = repository.getReferencesDirectory().resolve(currentBranch);
            if (!Files.exists(reference)) {
                throw new HeadFileCorruptException();
            }
            currentRevisionSha = Files.readAllLines(reference).get(0);
        } else {
            currentRevisionSha = currentHead;
        }
        if (!repository.isValidSha(currentRevisionSha)) {
            throw new RepositoryCorruptException();
        }

        VCSCommit currentRevision = readCommit(repository, currentRevisionSha);
        VCSCommit newRevision = readCommit(repository, newRevisionSha);
        VCSTree currentTree = readTree(repository, currentRevision.getTreeSha());
        VCSTree newTree = readTree(repository, newRevision.getTreeSha());
        Set<Path> removedFiles = walkVcsTree(currentTree).keySet();
        Map<Path, String> addedFiles = walkVcsTree(newTree);
        for (Path removedFile : removedFiles) {
            Files.delete(repository.getRootDirectory().resolve(removedFile));
        }
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                throw new RepositoryCorruptException();
            }
            Files.copy(repository.getObject(sha), repository.getRootDirectory().resolve(addedFile));
        }
        updateIndex(repository, addedFiles, removedFiles);
        // update HEAD
        Files.write(repository.getHeadFile(),
                ((Files.exists(possibleReference) ? Constants.REFERENCE_HEAD_PREFIX : "") + name).getBytes());
    }

    public static void mergeCommit(@NotNull Path path, @NotNull String name) {
        //TODO: implement merge
        throw new NotImplementedException();
    }
}
