package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.Assert.assertThat;

public class NucleusManagerTest {
    private static final int OBJECT_DIRECTORY_NAME_LENGTH = 2;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path temporaryRootPath;
    private NucleusRepository repository;

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
}