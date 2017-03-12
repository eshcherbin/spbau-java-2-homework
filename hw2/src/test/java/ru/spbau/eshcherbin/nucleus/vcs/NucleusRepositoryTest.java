package ru.spbau.eshcherbin.nucleus.vcs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.DirectoryIteratorException;
import java.nio.file.Path;
import java.sql.NClob;

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

    @Test(expected = DirectoryExpectedException.class)
    public void findRepositoryDirectoryExpectedTest() throws Exception {
        NucleusRepository.findRepository(temporaryFolder.newFile().toPath());
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
}