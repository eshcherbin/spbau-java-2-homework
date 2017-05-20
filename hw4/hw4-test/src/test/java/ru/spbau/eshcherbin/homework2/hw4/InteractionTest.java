package ru.spbau.eshcherbin.homework2.hw4;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.spbau.eshcherbin.hw4.client.FtpClient;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponseItem;
import ru.spbau.eshcherbin.hw4.server.FtpServer;
import ru.spbau.eshcherbin.hw4.server.Server;

import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class InteractionTest {
    private static final @NotNull Random random = new Random();
    private static final @NotNull Server server = new FtpServer(new InetSocketAddress(ConnectionTest.PORT - 1));

    private @Nullable Path file1;
    private @Nullable byte[] bytes;

    public @Rule TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        server.start();
        Thread.sleep(100);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Thread.sleep(100);
        server.stop();
    }

    @Before
    public void setUp() throws Exception {
        file1 = temporaryFolder.newFile("file1").toPath();
        temporaryFolder.newFile("file2");
        temporaryFolder.newFolder("folder1");

        bytes = new byte[1024];
        random.nextBytes(bytes);
        FileOutputStream fileOutputStream = new FileOutputStream(file1.toFile());
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }

    @Test
    public void listTest() throws Exception {
        FtpClient client = new FtpClient();
        client.connect(new InetSocketAddress("127.0.0.1", ConnectionTest.PORT - 1));
        final FtpListResponse response = client.executeList(temporaryFolder.getRoot().toString());
        client.disconnect();

        final List<FtpListResponseItem> responseItems = response.getResponseItems();
        assertThat(responseItems.size(), is(3));
        responseItems.sort(Comparator.comparing(FtpListResponseItem::getFileName));

        FtpListResponseItem item0 = responseItems.get(0);
        assertThat(item0.getFileName(), is("file1"));
        assertThat(item0.isDirectory(), is(false));

        FtpListResponseItem item1 = responseItems.get(1);
        assertThat(item1.getFileName(), is("file2"));
        assertThat(item1.isDirectory(), is(false));

        FtpListResponseItem item2 = responseItems.get(2);
        assertThat(item2.getFileName(), is("folder1"));
        assertThat(item2.isDirectory(), is(true));
    }

    @Test
    public void getTest() throws Exception {
        FtpClient client = new FtpClient();
        client.connect(new InetSocketAddress("127.0.0.1", ConnectionTest.PORT - 1));
        final Path file3 = temporaryFolder.getRoot().toPath().resolve("file3");
        client.executeGet(file1.toString(), file3);
        assertThat(Files.readAllBytes(file3), is(bytes));
        client.disconnect();
    }
}
