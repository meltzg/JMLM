package org.meltzg.jmlm.device;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class FileSystemAudioContentDeviceTest {


    protected static final String RESOURCEDIR = "./src/test/resources";
    protected static final String TMPDIR = RESOURCEDIR + "/temp";

    private FileSystemAudioContentDevice device;

    @Before
    public void before() throws IOException {
        this.device = new FileSystemAudioContentDevice();
        FileUtils.forceMkdir(Paths.get(TMPDIR).toFile());
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(Paths.get(TMPDIR).toFile());
    }

    @Test
    public void testAddLibraryRoot() throws FileNotFoundException {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);
        var expectedDevice = device.getGson().fromJson(new FileReader(RESOURCEDIR + "/audio/jst2018-12-09.json"), FileSystemAudioContentDevice.class);

        assertTrue(expectedDevice.getLibraryRoots().values().containsAll(device.getLibraryRoots().values()));
        assertEquals(1, device.getStorageDevices().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices()).get(0);
        var libraryRootFile = new File(libraryRootPath);
        assertTrue(storageDevice.getCapacity() >= libraryRootFile.getFreeSpace());
        assertTrue(storageDevice.getCapacity() < libraryRootFile.getTotalSpace());
        assertEquals(expectedDevice.getContent(), device.getContent());
    }

    @Test
    public void testAddMultipleLibraryRoot() throws FileNotFoundException {
        String[] libraryRoots = {
                RESOURCEDIR + "/audio/jst2018-12-09",
                RESOURCEDIR + "/audio/kwgg2016-10-29"
        };

        for (var root : libraryRoots) {
            device.addLibraryRoot(root, false);
        }
        device.scanDeviceContent();

        assertEquals(libraryRoots.length, device.getLibraryRoots().size());
        for (var root : device.getLibraryRoots().values()) {
            assertTrue(Arrays.stream(libraryRoots).anyMatch(root::endsWith));
        }
        assertEquals(1, device.getStorageDevices().size());
        assertEquals(2, device.getLibraryRootCapacities().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices()).get(0);
        assertTrue(Math.abs(storageDevice.getCapacity() - device.getLibraryRootCapacities().values()
                .stream().mapToLong(Long::longValue).sum()) <= 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddParentOfLibraryRoot() {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(libraryRootPath);
        device.addLibraryRoot(parentLibraryPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChildOfLibraryRoot() {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(parentLibraryPath);
        device.addLibraryRoot(libraryRootPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNotDirectoryLibraryRoot() {
        device.addLibraryRoot(RESOURCEDIR + "/audio/jst2018-12-09.json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNotExistLibraryRoot() {
        device.addLibraryRoot(RESOURCEDIR + "/audio/NotFound");
    }

    @Test
    public void testSerialization() {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);
        var deserialized = device.getGson().fromJson(device.getGson().toJson(device), FileSystemAudioContentDevice.class);

        assertEquals(device, deserialized);
    }

    @Test
    public void testMoveToDevice() {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        try (var isfs = Files.newInputStream(Paths.get(testFile))) {
            var content = device.addContentToDevice(isfs, testSubLibPath,
                    device.getLibraryRoots().keySet().iterator().next());
            assertTrue(device.getContent().containsKey(content.getId()));
        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = CannotReadException.class)
    public void testMoveInvalidTypeToDevice() throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09.json";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        var isfs = Files.newInputStream(Paths.get(testFile));
        var content = device.addContentToDevice(isfs, testSubLibPath,
                device.getLibraryRoots().keySet().iterator().next());
        assertNull(content);
        assertTrue(!Paths.get(TMPDIR, testSubLibPath).toFile().exists());
    }

    @Test
    public void testMoveDirectoryToDevice() {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        try (var isfs = Files.newInputStream(Paths.get(testFile))) {
            device.addContentToDevice(isfs, testSubLibPath,
                    device.getLibraryRoots().keySet().iterator().next());
            assertTrue(!Paths.get(TMPDIR, testSubLibPath).toFile().exists());
        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException e) {
            assertEquals("Is a directory", e.getMessage());
        }
    }

    @Test
    public void testMoveDuplicateToDevice()  {
        var tmpRoot1 = Paths.get(TMPDIR, "1");
        var tmpRoot2 = Paths.get(TMPDIR, "2");

        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        try (var isfs1 = Files.newInputStream(Paths.get(testFile));
             var isfs2 = Files.newInputStream(Paths.get(testFile))) {
            FileUtils.forceMkdir(tmpRoot1.toFile());
            FileUtils.forceMkdir(tmpRoot2.toFile());

            device.addLibraryRoot(tmpRoot1.toString());
            device.addLibraryRoot(tmpRoot2.toString());

            var libIds = new ArrayList<>(device.getLibraryRoots().keySet());

            device.addContentToDevice(isfs1, testSubLibPath, libIds.get(0));
            device.addContentToDevice(isfs2, testSubLibPath, libIds.get(1));
        } catch (FileAlreadyExistsException e) {
            assertTrue(true);
        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDeleteContent() {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));

        try (var isfs = Files.newInputStream(Paths.get(testFile))) {
            var content = device.addContentToDevice(isfs, testSubLibPath,
                    device.getLibraryRoots().keySet().iterator().next());
            device.deleteContent(content.getId());
            assertTrue(!device.getContent().containsKey(content.getId()));
        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testDeleteContentNotFound() throws IOException {
        device.addLibraryRoot(TMPDIR);
        device.deleteContent("doesn't exist");
    }
}