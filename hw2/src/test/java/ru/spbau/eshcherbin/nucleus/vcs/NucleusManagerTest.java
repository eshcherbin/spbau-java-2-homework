package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.base.Splitter;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.Assert.assertThat;

public class NucleusManagerTest {
    private static final int OBJECT_DIRECTORY_NAME_LENGTH = 2;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path temporaryRootPath;
    private NucleusRepository repository;

    private @NotNull VCSTree readTree(@NotNull NucleusRepository repository,
                                      @NotNull String treeSha,
                                      @NotNull String treeName)
            throws IOException {
        List<String> treeLines = Files.readAllLines(repository.getObject(treeSha));
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        VCSTree tree = new VCSTree(treeName);
        for (String line : treeLines) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            assertThat(splitResults.size(), is(3));
            VCSObjectType type = VCSObjectType.valueOf(splitResults.get(0));
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            assertThat(repository.isValidSha(sha), is(true));
            assertThat(type, anyOf(is(VCSObjectType.BLOB), is(VCSObjectType.TREE)));
            if (type == VCSObjectType.BLOB) {
                tree.addChild(new VCSObjectWithNameAndKnownSha(name, sha, type));
            } else {
                tree.addChild(readTree(repository, sha, treeName));
            }
        }
        return tree;
    }

    private @NotNull VCSTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha)
            throws IOException {
        return readTree(repository, treeSha, "");
    }

    @Before
    public void setUp() throws Exception {
        temporaryRootPath = temporaryFolder.getRoot().toPath();
    }

    @Test
    public void initializationTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        assertThat(repository.getObjectsDirectory().toFile(), is(anExistingDirectory()));
        assertThat(repository.getReferencesDirectory().toFile(), is(anExistingDirectory()));
        assertThat(repository.getIndexFile().toFile(), is(anExistingFile()));
        assertThat(repository.getHeadFile().toFile(), is(anExistingFile()));
    }

    @Test(expected = RepositoryNotInitializedException.class)
    public void addToIndexNoRepositoryTest() throws Exception {
        Path file = temporaryFolder.newFile().toPath();
        Files.write(file, "testContent".getBytes());
        NucleusManager.addToIndex(file);
    }

    @Test(expected = IndexFileCorruptException.class)
    public void addToIndexCorruptIndexTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        Files.write(repository.getIndexFile(), "someJunk".getBytes());
        Path file = temporaryFolder.newFile().toPath();
        Files.write(file, "testContent".getBytes());
        NucleusManager.addToIndex(file);
    }

    @Test
    public void addToIndexTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        Path file = temporaryFolder.newFile().toPath();
        byte[] content = "testContent".getBytes();
        Files.write(file, content);
        NucleusManager.addToIndex(file);
        HashFunction hashFunction = Hashing.sha1();
        String sha = hashFunction.newHasher().putBytes(content).hash().toString();
        Path objectDirectory = repository.getObjectsDirectory().resolve(sha.substring(0, OBJECT_DIRECTORY_NAME_LENGTH));
        assertThat(objectDirectory.toFile(), is(anExistingDirectory()));
        Path objectFile = objectDirectory.resolve(sha.substring(OBJECT_DIRECTORY_NAME_LENGTH));
        assertThat(objectFile.toFile(), is(anExistingFile()));
        assertThat(Files.readAllBytes(objectFile), is(content));
        List<String> indexLines = Files.readAllLines(repository.getIndexFile());
        assertThat(indexLines.size(), is(1));
        assertThat(indexLines.get(0), is(repository.getRootDirectory().relativize(file).toString()
                + '\t' + sha));
    }

    @Test
    public void removeFromIndexTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        Path file = temporaryFolder.newFile().toPath();
        byte[] content = "testContent".getBytes();
        Files.write(file, content);
        NucleusManager.addToIndex(file);
        NucleusManager.removeFromIndex(file);
        assertThat(file.toFile(), is(not(anExistingFile())));
        List<String> indexLines = Files.readAllLines(repository.getIndexFile());
        assertThat(indexLines.isEmpty(), is(true));
    }

    @Test
    public void commitChangesTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        Path file = temporaryFolder.newFile().toPath();
        byte[] content = "testContent".getBytes();
        Files.write(file, content);
        NucleusManager.addToIndex(file);
        NucleusManager.commitChanges(temporaryRootPath, "test commit message");
        String currentHead = repository.getCurrentHead();
        assertThat(currentHead, is(Constants.REFERENCE_HEAD_PREFIX + Constants.DEFAULT_BRANCH_NAME));
        String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
        List<String> currentHeadLines = Files.readAllLines(repository.getReferencesDirectory().resolve(currentBranch));
        assertThat(currentHeadLines.size(), is(1));
        String sha = currentHeadLines.get(0);

        assertThat(repository.isValidSha(sha), is(true));
        List<String> commitLines = Files.readAllLines(repository.getObject(sha));
        assertThat(commitLines.size(), is(4));
        String treeSha = commitLines.get(0);
        String author = commitLines.get(1);
        String timeString = commitLines.get(2);
        String message = commitLines.get(3);
        assertThat(repository.isValidSha(treeSha), is(true));
        assertThat(author, is(System.getProperty(Constants.USER_NAME_PROPERTY)));
        long time = Long.parseLong(timeString);
        assertThat(message, is("test commit message"));

        assertThat(repository.isValidSha(treeSha), is(true));
        VCSTree tree = readTree(repository, treeSha);

        assertThat(tree.children.size(), is(1));
        VCSObjectWithName vcsObjectWithName = tree.children.stream().findAny().orElseThrow(Exception::new);
        assertThat(vcsObjectWithName.getType(), is(VCSObjectType.BLOB));
        assertThat(vcsObjectWithName.getName(), is(file.getFileName().toString()));
        assertThat(repository.isValidSha(vcsObjectWithName.getSha()), is(true));
    }

    @Test
    public void severalCommitsTest() throws Exception {
        repository = NucleusManager.initRepository(temporaryRootPath);
        Path file1 = temporaryFolder.newFile().toPath();
        byte[] content1 = "testContent1".getBytes();
        Files.write(file1, content1);
        NucleusManager.addToIndex(file1);
        NucleusManager.commitChanges(temporaryRootPath, "test commit message");

        String firstCommitSha;
        {
            String currentHead = repository.getCurrentHead();
            String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
            List<String> currentHeadLines = Files.readAllLines(repository.getReferencesDirectory().resolve(currentBranch));
            firstCommitSha = currentHeadLines.get(0);
        }

        Path file2 = temporaryFolder.newFile().toPath();
        byte[] content2 = "testContent2".getBytes();
        Files.write(file2, content2);
        NucleusManager.addToIndex(file2);
        NucleusManager.commitChanges(temporaryRootPath, "another test commit message");

        String currentHead = repository.getCurrentHead();
        assertThat(currentHead, is(Constants.REFERENCE_HEAD_PREFIX + Constants.DEFAULT_BRANCH_NAME));
        String currentBranch = currentHead.substring(Constants.REFERENCE_HEAD_PREFIX.length());
        List<String> currentHeadLines = Files.readAllLines(repository.getReferencesDirectory().resolve(currentBranch));
        assertThat(currentHeadLines.size(), is(1));
        String sha = currentHeadLines.get(0);

        assertThat(repository.isValidSha(sha), is(true));
        List<String> commitLines = Files.readAllLines(repository.getObject(sha));
        assertThat(commitLines.size(), is(5));
        String treeSha = commitLines.get(0);
        String author = commitLines.get(1);
        String timeString = commitLines.get(2);
        String parent = commitLines.get(3);
        String message = commitLines.get(4);
        assertThat(repository.isValidSha(treeSha), is(true));
        assertThat(author, is(System.getProperty(Constants.USER_NAME_PROPERTY)));
        long time = Long.parseLong(timeString);
        assertThat(parent, is(Constants.PARENT_COMMIT_PREFIX + firstCommitSha));
        assertThat(message, is("another test commit message"));
    }
}