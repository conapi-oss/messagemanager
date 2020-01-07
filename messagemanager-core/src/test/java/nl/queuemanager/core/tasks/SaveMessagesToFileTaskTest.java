package nl.queuemanager.core.tasks;

import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.impl.MessageFactory;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;
import static org.junit.Assert.*;

public class SaveMessagesToFileTaskTest {

    @Test
    public void testSaveMultipleFiles() throws IOException, JMSException {
        Path tempDir = Files.createTempDirectory("test");
        long timestamp = 1578276704L;
        String uuid = UUID.randomUUID().toString();

        JMSMultipartMessage message = MessageFactory.createMultipartMessage();
        message.setJMSTimestamp(timestamp);
        message.setJMSMessageID(uuid);
        message.addPart(message.createPart("Some text", "text/plain"));
        message.addPart(message.createPart("<xml/>", "text/xml"));
        message.addPart(message.createPart("[1,2,3]", "application/json"));

        try {
            SaveMessagesToFileTask.saveMultipleFiles(message, tempDir.toFile());

            Path part0 = tempDir.resolve(timestamp + "-" + uuid + "_PART0.txt");
            assertTrue("Part 0 must exist", Files.exists(part0));
            assertArrayEquals("Some text".getBytes(), Files.readAllBytes(part0));

            Path part1 = tempDir.resolve(timestamp + "-" + uuid + "_PART1.xml");
            assertTrue("Part 1 must exist", Files.exists(part1));
            assertArrayEquals("<xml/>".getBytes(), Files.readAllBytes(part1));

            Path part2 = tempDir.resolve(timestamp + "-" + uuid + "_PART2.bin");
            assertTrue("Part 2 must exist", Files.exists(part2));
            assertArrayEquals("[1,2,3]".getBytes(), Files.readAllBytes(part2));
        } finally {
            recursiveDelete(tempDir);
        }
    }

    @Test
    public void testSaveMultipleFilesToNonExistentDir() throws JMSException, IOException {
        Path tempDir = Files.createTempDirectory("test");
        Files.delete(tempDir);

        long timestamp = 1578276704L;
        String uuid = UUID.randomUUID().toString();

        JMSMultipartMessage message = MessageFactory.createMultipartMessage();
        message.setJMSTimestamp(timestamp);
        message.setJMSMessageID(uuid);
        message.addPart(message.createPart("Some text", "text/plain"));
        message.addPart(message.createPart("<xml/>", "text/xml"));
        message.addPart(message.createPart("[1,2,3]", "application/json"));

        try {
            SaveMessagesToFileTask.saveMultipleFiles(message, tempDir.toFile());

            Path part0 = tempDir.resolve(timestamp + "-" + uuid + "_PART0.txt");
            assertTrue("Part 0 must exist", Files.exists(part0));
            assertArrayEquals("Some text".getBytes(), Files.readAllBytes(part0));

            Path part1 = tempDir.resolve(timestamp + "-" + uuid + "_PART1.xml");
            assertTrue("Part 1 must exist", Files.exists(part1));
            assertArrayEquals("<xml/>".getBytes(), Files.readAllBytes(part1));

            Path part2 = tempDir.resolve(timestamp + "-" + uuid + "_PART2.bin");
            assertTrue("Part 2 must exist", Files.exists(part2));
            assertArrayEquals("[1,2,3]".getBytes(), Files.readAllBytes(part2));
        } finally {
            recursiveDelete(tempDir);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testSaveMultipleFilesNameCollision() throws IOException, JMSException {
        Path tempPath = Files.createTempDirectory("test");
        Files.delete(tempPath);

        Files.write(tempPath, new byte[] {1,2,3,4,5});
        tempPath.toFile().deleteOnExit();

        SaveMessagesToFileTask.saveMultipleFiles(null, tempPath.toFile());
    }

    @Test
    public void testCreateFilenameForMessage() throws JMSException {
        File parent = new File("parent");
        long timestamp = 1578276704L;
        String uuid = UUID.randomUUID().toString();

        Message message = MessageFactory.createMessage();
        message.setJMSTimestamp(timestamp);
        message.setJMSMessageID(uuid);
        File file = SaveMessagesToFileTask.createFilenameForMessage(message, parent);

        assertEquals(timestamp + "-" + uuid, file.getName());
        assertEquals(parent, file.getParentFile());
    }

    private static void recursiveDelete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}