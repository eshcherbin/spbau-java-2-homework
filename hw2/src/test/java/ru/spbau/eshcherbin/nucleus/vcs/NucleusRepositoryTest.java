package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.junit.Assert.assertThat;

public class NucleusRepositoryTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path temporaryRootPath;

    @Before
    public void setUp() throws Exception {
        temporaryRootPath = temporaryFolder.getRoot().toPath();
    }

    @Test
    public void createRepositoryTest() throws Exception {
        NucleusRepository repository = NucleusRepository.createRepository(temporaryRootPath);
        assertThat(repository.getRootDirectory(), is(temporaryRootPath));
        assertThat(repository.getRepositoryDirectory(),
                is(temporaryRootPath.resolve(Constants.REPOSITORY_DIRECTORY_NAME)));
        assertThat(repository.getRepositoryDirectory().toFile(), is(anExistingDirectory()));
    }

    @Test(expected = RepositoryAlreadyInitializedException.class)
    public void createRepositoryAlreadyInitializedTest() throws Exception {
        NucleusRepository.createRepository(temporaryRootPath);
        NucleusRepository.createRepository(temporaryFolder.newFolder().toPath());
    }

    @Test(expected = DirectoryExpectedException.class)
    public void createRepositoryDirectoryExpectedTest() throws Exception {
        NucleusRepository.createRepository(temporaryFolder.newFile().toPath());
    }

    @Test
    public void findRepositoryTest() throws Exception {
        Path folder = temporaryFolder.newFolder().toPath();
        NucleusRepository repository = NucleusRepository.createRepository(folder);
        assertThat(NucleusRepository.findRepository(temporaryRootPath), is(nullValue()));
        assertThat(NucleusRepository.findRepository(folder), is(repository));
    }

    @Test
    public void innerStructureTest() throws Exception {
        NucleusRepository repository = NucleusRepository.createRepository(temporaryRootPath);
        Path repositoryDirectory = repository.getRepositoryDirectory();
        assertThat(repository.getObjectsDirectory(), is(repositoryDirectory.resolve(Constants.OBJECTS_DIRECTORY_NAME)));
        assertThat(repository.getReferencesDirectory(), is(repositoryDirectory.resolve(Constants.REFERENCES_DIRECTORY_NAME)));
        assertThat(repository.getIndexFile(), is(repositoryDirectory.resolve(Constants.INDEX_FILE_NAME)));
        assertThat(repository.getHeadFile(), is(repositoryDirectory.resolve(Constants.HEAD_FILE_NAME)));
    }

    @Test
    public void getObjectTest() throws Exception {
        NucleusRepository repository = NucleusManager.initializeRepository(temporaryRootPath);
        HashFunction sha1 = Hashing.sha1();
        byte[] bytes = new byte[20];
        new Random().nextBytes(bytes);
        String sha = sha1.newHasher().putBytes(bytes).hash().toString();
        Path expectedDirectory =
                repository.getObjectsDirectory().resolve(sha.substring(0, Constants.OBJECT_DIRECTORY_NAME_LENGTH));
        Path expectedName = expectedDirectory.resolve(sha.substring(Constants.OBJECT_DIRECTORY_NAME_LENGTH));
        assertThat(repository.getObject(sha), is(expectedName));
    }

    @Test
    public void isValidShaTest() throws Exception {
        NucleusRepository repository = NucleusManager.initializeRepository(temporaryRootPath);
        HashFunction sha1 = Hashing.sha1();
        byte[] bytes = new byte[20];
        new Random().nextBytes(bytes);
        String sha = sha1.newHasher().putBytes(bytes).hash().toString();
        Files.createDirectory(repository.getObject(sha).getParent());
        assertThat(repository.isValidSha(sha), is(false));
        Files.createFile(repository.getObject(sha));
        assertThat(repository.isValidSha(sha), is(true));
        assertThat(repository.isValidSha(sha + 'a'), is(false));
        assertThat(repository.isValidSha("af"), is(false));
    }

    @Test(expected = HeadFileCorruptException.class)
    public void getCurrentHeadNoHeadFileTest() throws Exception {
        NucleusRepository repository = NucleusManager.initializeRepository(temporaryRootPath);
        Files.delete(repository.getHeadFile());
        repository.getCurrentHead();
    }

    @Test
    public void getCurrentHeadDefaultTest() throws Exception {
        NucleusRepository repository = NucleusManager.initializeRepository(temporaryRootPath);
        assertThat(repository.getCurrentHead(), is(Constants.REFERENCE_HEAD_PREFIX + Constants.DEFAULT_BRANCH_NAME));
    }
}