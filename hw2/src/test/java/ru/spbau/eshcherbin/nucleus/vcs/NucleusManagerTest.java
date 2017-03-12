package ru.spbau.eshcherbin.nucleus.vcs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.Assert.assertThat;

public class NucleusManagerTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path temporaryRootPath;

    @Before
    public void setUp() throws Exception {
        temporaryRootPath = temporaryFolder.getRoot().toPath();
    }

    @Test
    public void initializationTest() throws Exception {
        NucleusRepository repository = NucleusManager.initRepository(temporaryRootPath);
        assertThat(repository.getObjectsDirectory().toFile(), is(anExistingDirectory()));
        assertThat(repository.getReferencesDirectory().toFile(), is(anExistingDirectory()));
        assertThat(repository.getIndexFile().toFile(), is(anExistingFile()));
        assertThat(repository.getHeadFile().toFile(), is(anExistingFile()));
    }
}