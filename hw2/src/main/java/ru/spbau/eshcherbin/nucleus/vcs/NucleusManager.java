package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Managing class that provides all the VCS functionality via stateless static methods.
 */
public class NucleusManager {
    private static final Logger logger = LoggerFactory.getLogger(NucleusManager.class);
    private static final Marker fatalMarker = MarkerFactory.getMarker("FATAL");

    private static final int BUFFER_SIZE = 4096;

    /**
     * Writes an object to the <tt>objects</tt> directory.
     * @param repository the operated repository
     * @param object the added object
     * @return the written object's sha
     * @throws IOException is an I/O error occurs
     */
    private static String addVcsObject(@NotNull NucleusRepository repository, @NotNull VcsObject object)
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

    /**
     * Writes a file to the <tt>objects</tt> directory.
     * @param repository the operated repository
     * @param filePath the path to the added file
     * @return the written object's sha
     * @throws IOException is an I/O error occurs
     */
    private static String addFile(@NotNull NucleusRepository repository, @NotNull Path filePath) throws IOException {
        return addVcsObject(repository, new VcsBlob(Files.readAllBytes(filePath), filePath.getFileName().toString()));
    }

    /**
     * Reads all files in current working copy from the index file into a map from file's path to its sha.
     * @param repository the operated repository
     * @return a map from file's path to its sha
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws IOException if an I/O error occurs
     */
    private static @NotNull Map<Path, String> readIndexFile(@NotNull NucleusRepository repository)
            throws IndexFileCorruptException, IOException {
        logger.debug("Reading index file");
        Map<Path, String> index = new HashMap<>();
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        if (!Files.exists(repository.getIndexFile())) {
            logger.error(fatalMarker, "No index file found");
            throw new IndexFileCorruptException();
        }
        for (String line : Files.readAllLines(repository.getIndexFile())) {
            List<String> splitResult = onTabSplitter.splitToList(line);
            if (splitResult.size() != 2 || !repository.isValidSha(splitResult.get(1))) {
                logger.error(fatalMarker, "Invalid line in index file: '{}'", line);
                throw new IndexFileCorruptException();
            }
            try {
                index.put(Paths.get(splitResult.get(0)), splitResult.get(1));
            } catch (InvalidPathException e) {
                logger.error(fatalMarker, "Invalid path in index file: '{}'", splitResult.get(0));
                throw new IndexFileCorruptException();
            }
        }
        return index;
    }

    /**
     * Updates the index file by adding and removing specified entries.
     * @param repository the operated repository
     * @param addedFiles the files that are to be added to the index
     * @param removedFiles the files that are to be removed from the index
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws IOException if an I/O error occurs
     */
    private static void updateIndex(@NotNull NucleusRepository repository, @NotNull Map<Path, String> addedFiles,
                                    @NotNull Set<Path> removedFiles)
            throws IOException, IndexFileCorruptException {
        logger.debug("Updating index file");
        Map<Path, String> index = readIndexFile(repository);
        index.keySet().removeAll(removedFiles);
        index.putAll(addedFiles);
        Files.write(repository.getIndexFile(), index.entrySet().stream()
                .map(entry -> entry.getKey().toString() + '\t' + entry.getValue())
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Reads the index file and constructs a tree of current working copy files.
     * @param repository the operated repository
     * @return a tree of current working copy files
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws IOException if an I/O error occurs
     */
    private static @NotNull VcsTree collectTreeFromIndex(@NotNull NucleusRepository repository)
            throws IndexFileCorruptException, IOException {
        logger.debug("Collecting a tree from index file");
        Map<Path, String> index = readIndexFile(repository);
        Map<Path, VcsTree> pathToTree = new HashMap<>();
        pathToTree.put(Paths.get(""), new VcsTree(""));
        for (Path path : index.keySet()) {
            for (Path prefixPath : path) {
                Path parent = prefixPath.getParent();
                if (parent == null) {
                    parent = Paths.get("");
                }
                Path name = prefixPath.getFileName();
                if (prefixPath.equals(path)) {
                    pathToTree.get(parent).addChild(
                            new VcsObjectWithNameAndKnownSha(
                                    name.toString(),
                                    index.get(path),
                                    VcsObjectType.BLOB
                            )
                    );
                } else if (!pathToTree.containsKey(prefixPath)) {
                    pathToTree.put(prefixPath, new VcsTree(name.toString()));
                }
            }
        }
        return pathToTree.get(Paths.get(""));
    }

    /**
     * Reads a commit from a file.
     * @param repository the operated repository
     * @param commitSha the commit's sha
     * @return the commit
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws IOException if an I/O error occurs
     */
    private static @NotNull VcsCommit readCommit(@NotNull NucleusRepository repository, @NotNull String commitSha)
            throws RepositoryCorruptException, IOException {
        logger.debug("Reading a commit object");
        if (!repository.isValidSha(commitSha)) {
            logger.error(fatalMarker, "No such object exists: {}", commitSha);
            throw new RepositoryCorruptException();
        }
        List<String> commitLines = Files.readAllLines(repository.getObject(commitSha));
        if (commitLines.size() < 4) {
            logger.error(fatalMarker, "Too few lines in commit object");
            throw new RepositoryCorruptException();
        }
        String treeSha = commitLines.get(0);
        if (!repository.isValidSha(treeSha)) {
            logger.error(fatalMarker, "No such object exists: {}", treeSha);
            throw new RepositoryCorruptException();
        }
        String author = commitLines.get(1);
        String timeInMillisecondsString = commitLines.get(2);
        long timeInMilliseconds;
        try {
             timeInMilliseconds = Long.parseLong(timeInMillisecondsString);
        } catch (NumberFormatException e) {
            logger.error(fatalMarker, "Invalid number: {}", timeInMillisecondsString);
            throw new RepositoryCorruptException();
        }
        int messageStartLineIndex = 3;
        while (messageStartLineIndex < commitLines.size() &&
                !commitLines.get(messageStartLineIndex).startsWith(Constants.MESSAGE_COMMIT_PREFIX)) {
            ++messageStartLineIndex;
        }
        if (messageStartLineIndex == commitLines.size()) {
            logger.error(fatalMarker, "No message in commit object");
            throw new RepositoryCorruptException();
        }
        StringBuilder messageBuilder = new StringBuilder(commitLines.get(messageStartLineIndex)
                .substring(Constants.MESSAGE_COMMIT_PREFIX.length()));
        for (int i = messageStartLineIndex + 1; i < commitLines.size(); ++i) {
            messageBuilder.append('\n');
            messageBuilder.append(commitLines.get(i));
        }
        String message = messageBuilder.toString();
        VcsCommit commit = new VcsCommit(treeSha, message, author, timeInMilliseconds);
        Set<String> parentShaSet = commit.getParents();
        for (int i = 3; i < messageStartLineIndex; ++i) {
            String line = commitLines.get(i);
            if (!line.startsWith(Constants.PARENT_COMMIT_PREFIX) ||
                    !repository.isValidSha(line.substring(Constants.PARENT_COMMIT_PREFIX.length()))) {
                logger.error(fatalMarker, "Invalid parent line in commit object");
                throw new RepositoryCorruptException();
            }
            parentShaSet.add(line.substring(Constants.PARENT_COMMIT_PREFIX.length()));
        }
        return commit;
    }

    /**
     * Reads a tree from a file.
     * @param repository the operated repository
     * @param treeSha the tree's sha
     * @return the tree
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws IOException if an I/O error occurs
     */
    private static @NotNull VcsTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha)
            throws RepositoryCorruptException, IOException {
        return readTree(repository, treeSha, "");
    }

    /**
     * Reads a tree from a file.
     * @param repository the operated repository
     * @param treeSha the tree's sha
     * @param treeName the tree's desired name
     * @return the tree
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws IOException if an I/O error occurs
     */
    private static @NotNull VcsTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha,
                                             @NotNull String treeName)
            throws RepositoryCorruptException, IOException {
        logger.debug("Reading a tree object");
        if (!repository.isValidSha(treeSha)) {
            logger.error(fatalMarker, "No such object exists: {}", treeSha);
            throw new RepositoryCorruptException();
        }
        VcsTree tree = new VcsTree(treeName);
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        for (String line : Files.readAllLines(repository.getObject(treeSha))) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            if (splitResults.size() != 3) {
                logger.error(fatalMarker, "Invalid line in tree object: {}", line);
                throw new RepositoryCorruptException();
            }
            VcsObjectType type;
            try {
                type = VcsObjectType.valueOf(splitResults.get(0));
            } catch (IllegalArgumentException e) {
                logger.error(fatalMarker, "Invalid type line in tree object: {}", splitResults.get(0));
                throw new RepositoryCorruptException();
            }
            if (!(type == VcsObjectType.BLOB || type == VcsObjectType.TREE)) {
                logger.error(fatalMarker, "Invalid type in tree object: {}", type);
                throw new RepositoryCorruptException();
            }
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            if (type == VcsObjectType.BLOB) {
                if (!repository.isValidSha(sha)) {
                    logger.error(fatalMarker, "No such object exists: {}", sha);
                    throw new RepositoryCorruptException();
                }
                tree.addChild(new VcsObjectWithNameAndKnownSha(name, sha, type));
            } else {
                tree.addChild(readTree(repository, sha, treeName));
            }
        }
        return tree;
    }

    /**
     * Traverse the given tree and collects all files as a map from file's path to its sha.
     * @param tree the tree to be traversed
     * @return a map from file's path to its sha
     */
    private static @NotNull Map<Path, String> walkVcsTree(@NotNull VcsTree tree) {
        Map<Path, String> files = new HashMap<>();
        walkVcsTree(tree, Paths.get(""), files);
        return files;
    }

    /**
     * Traverse the given tree and collects all files into a given map from file's path to its sha.
     * @param tree the tree to be traversed
     * @param path the desired path of the root
     * @param filesMap the map to store the result
     */
    private static void walkVcsTree(@NotNull VcsTree tree, @NotNull Path path, @NotNull Map<Path, String> filesMap) {
        for (VcsObjectWithName object : tree.children) {
            if (object.getType() == VcsObjectType.BLOB) {
                filesMap.put(path.resolve(object.getName()), object.getSha());
            } else {
                walkVcsTree((VcsTree) object, path.resolve(object.getName()), filesMap);
            }
        }
    }

    /**
     * Copies the content of given revision into current working copy. Existing files are overwritten.
     * @param repository the operated repository
     * @param revisionName the deployed revision's name
     * @param removeCurrent should all files from current working copy be deleted before deployment
     * @return sha of the deployed revision
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws NoSuchRevisionOrBranchException if the given revision does not exist
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    private static @NotNull String deployRevision(@NotNull NucleusRepository repository, @NotNull String revisionName,
                                                  boolean removeCurrent)
            throws IOException, RepositoryNotInitializedException,
            RepositoryCorruptException, NoSuchRevisionOrBranchException, HeadFileCorruptException,
            IndexFileCorruptException {
        logger.debug("Deploying revision {}, removeCurrent = {}", revisionName, removeCurrent);
        String deployedRevisionSha;
        Path possibleReference = repository.getReferencesDirectory().resolve(revisionName);
        if (Files.exists(possibleReference)) {
            deployedRevisionSha = Files.readAllLines(possibleReference).get(0);
            if (!repository.isValidSha(deployedRevisionSha)) {
                logger.error(fatalMarker, "No such object exists: {}", deployedRevisionSha);
                throw new RepositoryCorruptException();
            }
        } else {
            deployedRevisionSha = revisionName;
            if (!repository.isValidSha(deployedRevisionSha)) {
                logger.error(fatalMarker, "No such object exists: {}", deployedRevisionSha);
                throw new NoSuchRevisionOrBranchException();
            }
        }
        String currentHead = repository.getCurrentHead();
        String currentRevisionSha;
        try {
            currentRevisionSha = repository.getRevisionSha(currentHead);
        } catch (HeadFileCorruptException e) {
            logger.error(fatalMarker, "HEAD file content is corrupt: {}", currentHead);
            throw e;
        }
        if (!repository.isValidSha(currentRevisionSha)) {
            logger.error(fatalMarker, "No such object exists: {}", currentRevisionSha);
            throw new RepositoryCorruptException();
        }

        Set<Path> removedFiles;
        if (removeCurrent) {
            VcsCommit currentRevision = readCommit(repository, currentRevisionSha);
            VcsTree currentTree = readTree(repository, currentRevision.getTreeSha());
            removedFiles = walkVcsTree(currentTree).keySet();
            for (Path removedFile : removedFiles) {
                Files.delete(repository.getRootDirectory().resolve(removedFile));
            }
        } else {
            removedFiles = Collections.emptySet();
        }
        VcsCommit deployedRevision = readCommit(repository, deployedRevisionSha);
        VcsTree deployedTree = readTree(repository, deployedRevision.getTreeSha());
        Map<Path, String> addedFiles = walkVcsTree(deployedTree);
        for (Map.Entry<Path, String> entry : addedFiles.entrySet()) {
            Path addedFile = entry.getKey();
            String sha = entry.getValue();
            if (!repository.isValidSha(sha)) {
                logger.error(fatalMarker, "No such object exists: {}", sha);
                throw new RepositoryCorruptException();
            }
            Files.copy(repository.getObject(sha), repository.getRootDirectory().resolve(addedFile), REPLACE_EXISTING);
        }
        updateIndex(repository, addedFiles, removedFiles);
        return deployedRevisionSha;
    }

    /**
     * Commit changes in current working copy.
     * @param path a path inside the repository
     * @param message commit message
     * @param additionalParentSha an additional (besides current revision) parent's sha or <tt>null</tt> if no
     *                            additional parent is needed
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    private static void commitChanges(@NotNull Path path, @NotNull String message, @Nullable String additionalParentSha)
            throws IOException, RepositoryNotInitializedException, HeadFileCorruptException,
            IndexFileCorruptException {
        logger.debug("Commiting changes");
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
            VcsTree tree = collectTreeFromIndex(repository);
            addVcsObject(repository, tree);
            VcsCommit commit = new VcsCommit(tree.getSha(), message, System.getProperty(Constants.USER_NAME_PROPERTY),
                    System.currentTimeMillis());
            if (parentSha != null) {
                commit.getParents().add(parentSha);
            }
            if (additionalParentSha != null) {
                commit.getParents().add(additionalParentSha);
            }
            addVcsObject(repository, commit);
            Files.write(repository.getHeadFile(), currentHead.getBytes());
            if (!Files.exists(reference)) {
                Files.createFile(reference);
            }
            Files.write(reference, commit.getSha().getBytes());
        } else {
            VcsTree tree = collectTreeFromIndex(repository);
            addVcsObject(repository, tree);
            VcsCommit commit = new VcsCommit(tree.getSha(), message, System.getProperty(Constants.USER_NAME_PROPERTY),
                    System.currentTimeMillis());
            commit.getParents().add(currentHead);
            if (additionalParentSha != null) {
                commit.getParents().add(additionalParentSha);
            }
            String commitSha = addVcsObject(repository, commit);
            Files.write(repository.getHeadFile(), commitSha.getBytes());
        }
    }

    /**
     * Returns a log of all commits preceding given revision.
     * @param path a path inside a repository
     * @param revisionName the revision to build the log from
     * @return the first log message of the log
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     */
    private static @Nullable LogMessage getLog(@NotNull Path path, @NotNull String revisionName)
            throws IOException, RepositoryNotInitializedException,
            RepositoryCorruptException {
        logger.debug("Constructing a revisions log for revision {}", revisionName);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String startCommitSha;
        if (revisionName.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            String branchName = revisionName.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            Path branchReference = repository.getReferencesDirectory().resolve(branchName);
            if (!Files.exists(branchReference)) {
                logger.error(fatalMarker, "No such reference exists: {}", branchReference);
                throw new RepositoryCorruptException();
            }
            startCommitSha = Files.readAllLines(branchReference).get(0);
        } else {
            startCommitSha = revisionName;
        }
        VcsCommit headCommit = readCommit(repository, startCommitSha);
        // breadth-first commit graph traversal
        Set<String> presentShaSet = new HashSet<>();
        presentShaSet.add(startCommitSha);
        Queue<VcsCommit> queue = new LinkedList<>();
        queue.add(headCommit);
        List<VcsCommit> commits = new ArrayList<>();
        while (!queue.isEmpty()) {
            VcsCommit commit = queue.remove();
            commits.add(commit);
            for (String parentSha : commit.getParents()) {
                if (!presentShaSet.contains(parentSha)) {
                    presentShaSet.add(parentSha);
                    queue.add(readCommit(repository, parentSha));
                }
            }
        }
        commits.sort(Comparator.comparingLong(VcsCommit::getTimeInMilliseconds));
        LogMessage currentLogMessage = null;
        for (VcsCommit commit : commits) {
            currentLogMessage = new LogMessage(commit, currentLogMessage);
        }
        return currentLogMessage;
    }

    private static @NotNull String calculateSha(@NotNull Path file) throws IOException {
        HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha1(), Files.newInputStream(file));
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        do {
            bytesRead = hashingInputStream.read(buffer, 0, BUFFER_SIZE);
        } while (bytesRead != -1);
        String sha = hashingInputStream.hash().toString();
        hashingInputStream.close();
        return sha;
    }

    /**
     * Initializes a repository at the given location.
     * @param path the location of the new repository
     * @return the repository
     * @throws IOException if an I/O error occurs
     * @throws DirectoryExpectedException if the path does not refer to a directory
     * @throws RepositoryAlreadyInitializedException if the path refers to a location inside an already existing
     *                                               repository
     */
    public static @NotNull NucleusRepository initializeRepository(@NotNull Path path)
            throws IOException, DirectoryExpectedException, RepositoryAlreadyInitializedException {
        logger.debug("Initializing a repository at {}", path);
        NucleusRepository repository = NucleusRepository.createRepository(path);
        Files.createDirectory(repository.getObjectsDirectory());
        Files.createDirectory(repository.getReferencesDirectory());
        Files.createFile(repository.getIndexFile());
        Files.createFile(repository.getHeadFile());
        return repository;
    }

    /**
     * Adds given file to current working copy.
     * @param path a path to the file that is to be added
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws IOException if an I/O error occurs
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    public static void addToIndex(@NotNull Path path)
            throws RepositoryNotInitializedException, IOException, IndexFileCorruptException {
        logger.debug("Adding file to index: {}", path);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository;
        repository = NucleusRepository.resolveRepository(path, true);
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

    /**
     * Removes given file from current working copy and from filesystem.
     * @param path a path to the file that is to be removed
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws IOException if an I/O error occurs
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    public static void removeFromIndex(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, IndexFileCorruptException {
        logger.debug("Removing file: {}", path);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository;
        repository = NucleusRepository.resolveRepository(path, true);
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

    /**
     * Commit changes in current working copy.
     * @param path a path inside the repository
     * @param message commit message
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    public static void commitChanges(@NotNull Path path, @NotNull String message)
            throws RepositoryNotInitializedException, IndexFileCorruptException,
            HeadFileCorruptException, IOException {
        commitChanges(path, message, null);
    }

    /**
     * Creates new branch with the given name.
     * @param path a path inside the repository
     * @param branchName the name of the new branch
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws BranchAlreadyExistsException if a branch with given name already exists
     */
    public static void createBranch(@NotNull Path path, @NotNull String branchName)
            throws IOException, RepositoryNotInitializedException,
            HeadFileCorruptException, BranchAlreadyExistsException {
        logger.debug("Creating branch: {}", branchName);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        Path newReference = repository.getReferencesDirectory().resolve(branchName);
        if (Files.exists(newReference)) {
            logger.error("Branch already exists: {}", newReference);
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

    /**
     * Deletes a branch with the given name.
     * @param path a path inside the repository
     * @param branchName the name of the deleted branch
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws NoSuchRevisionOrBranchException if no such branch exists
     * @throws DeletingHeadBranchException if the branch in question is the head branch
     */
    public static void deleteBranch(@NotNull Path path, @NotNull String branchName)
            throws IOException, RepositoryNotInitializedException,
            HeadFileCorruptException, NoSuchRevisionOrBranchException, DeletingHeadBranchException {
        logger.debug("Deleting branch: {}", branchName);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        Path reference = repository.getReferencesDirectory().resolve(branchName);
        if (!Files.exists(reference)) {
            logger.error(fatalMarker, "No such reference exists: {}", reference);
            throw new NoSuchRevisionOrBranchException();
        }
        String currentHead = repository.getCurrentHead();
        if (currentHead.equals(Constants.REFERENCE_HEAD_PREFIX + branchName)) {
            logger.error("Can't delete current branch");
            throw new DeletingHeadBranchException();
        }
        Files.delete(reference);
    }

    /**
     * Checks out the given revision.
     * @param path a path inside the repository
     * @param revisionName the name of the checked out revision
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws NoSuchRevisionOrBranchException if no such branch exists
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     */
    public static void checkoutRevision(@NotNull Path path, @NotNull String revisionName)
            throws IOException, RepositoryNotInitializedException,
            HeadFileCorruptException, RepositoryCorruptException, NoSuchRevisionOrBranchException,
            IndexFileCorruptException {
        logger.debug("Checkout revision: {}", revisionName);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        deployRevision(repository, revisionName, true);
        Path possibleReference = repository.getReferencesDirectory().resolve(revisionName);
        // update HEAD
        Files.write(repository.getHeadFile(),
                ((Files.exists(possibleReference) ? Constants.REFERENCE_HEAD_PREFIX : "") + revisionName).getBytes());
    }

    /**
     * Returns a log of all commits preceding current head revision.
     * @param path a path inside a repository
     * @return the first log message of the log
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     */
    public static @Nullable LogMessage getLog(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException,
            HeadFileCorruptException, RepositoryCorruptException {
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String currentHead = repository.getCurrentHead();
        return getLog(path, currentHead);
    }

    /**
     * Merges given revision into current working copy and creates a merge commit.
     * @param path a path inside the repository
     * @param revisionName the merged revision's name
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws NoSuchRevisionOrBranchException if the given revision does not exist
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     */
    public static void mergeRevision(@NotNull Path path, @NotNull String revisionName)
            throws IOException, RepositoryNotInitializedException, HeadFileCorruptException,
            IndexFileCorruptException, NoSuchRevisionOrBranchException, RepositoryCorruptException {
        logger.debug("Merge revision: {}", revisionName);
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String mergedRevisionSha = deployRevision(repository, revisionName, false);
        commitChanges(path, Constants.MERGE_COMMIT_MESSAGE + revisionName, mergedRevisionSha);
    }

    /**
     * Resets given file to current revision's initial state.
     * @param path the path ot the file
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws FileNotInRepositoryException if the specified file is not in the current revision
     */
    public static void resetFile(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, IndexFileCorruptException,
            FileNotInRepositoryException {
        logger.debug("Reset file: {}", path);
        path = path.toAbsolutePath();
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        Path relativePath = repository.getRootDirectory().relativize(path);
        Map<Path, String> index = readIndexFile(repository);
        if (!index.containsKey(relativePath)) {
            logger.debug("Index: {}", index);
            logger.error("No such file in index: {}", relativePath);
            throw new FileNotInRepositoryException();
        }
        String sha = index.get(relativePath);
        if (!repository.isValidSha(sha)) {
            logger.error(fatalMarker, "No such object exists: {}", sha);
            throw new IndexFileCorruptException();
        }
        Path object = repository.getObject(sha);
        logger.debug("Object's path is {} and file path is {}", object, path);
        Files.copy(object, path, REPLACE_EXISTING);
    }

    /**
     * Removes all files that are not added to the repository.
     * @param path a path inside the repository
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws IndexFileCorruptException if the index file's content is corrupt
     */
    public static void cleanRepository(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, IndexFileCorruptException {
        logger.debug("Clean repository");
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        Map<Path, String> index = readIndexFile(repository);
        Set<Path> allFiles = Files.walk(repository.getRootDirectory())
                .filter(file -> !file.startsWith(repository.getRepositoryDirectory())
                                && Files.isRegularFile(file))
                .map(file -> repository.getRootDirectory().relativize(file))
                .collect(Collectors.toSet());
        for (Path file : allFiles) {
            if (!index.containsKey(file)) {
                Files.deleteIfExists(repository.getRootDirectory().resolve(file));
            }
        }
    }

    /**
     * Returns current repository status, i.e. which files are added, modified, untracked, etc.
     * @param path a path inside the repository
     * @return current repository status
     * @throws IOException if an I/O error occurs
     * @throws RepositoryNotInitializedException if no repository containing <tt>path</tt> is found
     * @throws IndexFileCorruptException if the index file's content is corrupt
     * @throws HeadFileCorruptException if the HEAD file's content is corrupt
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     */
    public static RepositoryStatus getRepositoryStatus(@NotNull Path path)
            throws IOException, RepositoryNotInitializedException, HeadFileCorruptException,
            IndexFileCorruptException, RepositoryCorruptException {
        logger.debug("Collecting repository status");
        path = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        NucleusRepository repository = NucleusRepository.resolveRepository(path, false);
        String currentHead = repository.getCurrentHead();
        String revision;
        if (currentHead.startsWith(Constants.REFERENCE_HEAD_PREFIX)) {
            revision = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
        } else {
            revision = currentHead;
        }
        String revisionSha = repository.getRevisionSha(currentHead);
        VcsCommit commit = readCommit(repository, revisionSha);
        VcsTree tree = readTree(repository, commit.getTreeSha());
        Map<Path, String> treeContent = walkVcsTree(tree);
        Map<Path, String> index = readIndexFile(repository);
        RepositoryStatus status = new RepositoryStatus(revision);
        for (Path fileInIndex : index.keySet()) {
            if (!treeContent.containsKey(fileInIndex)) {
                status.addEntry(fileInIndex, StatusEntryType.ADDED);
            } else if (!treeContent.get(fileInIndex).equals(index.get(fileInIndex))) {
                status.addEntry(fileInIndex, StatusEntryType.MODIFIED);
            }
            if (!Files.exists(repository.getRootDirectory().resolve(fileInIndex))) {
                status.addEntry(fileInIndex, StatusEntryType.MISSING);
            }
        }
        for (Path fileInTree : treeContent.keySet()) {
            if (!index.containsKey(fileInTree)) {
                status.addEntry(fileInTree, StatusEntryType.REMOVED);
            }
        }
        Set<Path> allFiles = Files.walk(repository.getRootDirectory())
                .filter(file -> !file.startsWith(repository.getRepositoryDirectory())
                        && Files.isRegularFile(file))
                .map(file -> repository.getRootDirectory().relativize(file))
                .collect(Collectors.toSet());
        for (Path file : allFiles) {
            if (index.containsKey(file)) {
                String fileSha = calculateSha(file);
                if (!fileSha.equals(index.get(file))) {
                    status.addEntry(file, StatusEntryType.UNSTAGED);
                }
            } else {
                status.addEntry(file, StatusEntryType.UNTRACKED);
            }
        }
        return status;
    }
}
