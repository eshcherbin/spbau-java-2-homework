package ru.spbau.eshcherbin.hw4.messages;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MessageReaderAndWriterTest {
    private static final @NotNull Random random = new Random();

    public final @Rule TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readerTest() throws Exception {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(2032);
        byteBuffer.putInt(1024);
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        byteBuffer.put(bytes);
        byteBuffer.putInt(1024);
        byteBuffer.put(bytes, 0, 1000);
        byteBuffer.flip();

        final Path temporaryFilePath = temporaryFolder.newFile().toPath();
        FileChannel fileChannel = FileChannel.open(temporaryFilePath, StandardOpenOption.WRITE);
        fileChannel.write(byteBuffer);
        fileChannel.close();

        fileChannel = FileChannel.open(temporaryFilePath, StandardOpenOption.READ);
        MessageReader reader = new MessageReader(fileChannel);
        final Optional<Message> messageOptional = reader.read();
        assertThat(messageOptional.isPresent(), is(true));
        assertThat(messageOptional.get().getData(), is(bytes));
        final Optional<Message> secondMessageOptional = reader.read();
        assertThat(secondMessageOptional.isPresent(), is(false));
    }

    @Test
    public void writerTest() throws Exception {
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        final Message message = new Message(bytes);
        final Path temporaryFilePath = temporaryFolder.newFile().toPath();
        FileChannel fileChannel = FileChannel.open(temporaryFilePath, StandardOpenOption.WRITE);
        final MessageWriter writer = new MessageWriter(fileChannel);
        writer.startNewMessage(message);
        assertThat(writer.write(), is(true));
        fileChannel.close();

        final ByteBuffer byteBuffer = ByteBuffer.allocate(2032);
        fileChannel = FileChannel.open(temporaryFilePath, StandardOpenOption.READ);
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        assertThat(byteBuffer.getInt(), is(1024));
        byte[] bytes1 = new byte[1024];
        byteBuffer.get(bytes1);
        assertThat(bytes1, is(bytes));
        assertThat(byteBuffer.hasRemaining(), is(false));
    }
}