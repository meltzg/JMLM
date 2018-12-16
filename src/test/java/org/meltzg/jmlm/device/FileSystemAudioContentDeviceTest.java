package org.meltzg.jmlm.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meltzg.jmlm.device.content.AudioContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class FileSystemAudioContentDeviceTest {


    protected static final String RESOURCEDIR = "./src/test/resources";
    protected static final String TMPDIR = RESOURCEDIR + "/temp";

    private static Gson gson;

    private FileSystemAudioContentDevice device;

    @BeforeClass
    public static void beforeClass() {
        gson = new GsonBuilder()
                .registerTypeAdapter(FileSystemAudioContentDevice.class,
                        new FileSystemAudioContentDevice())
                .create();
    }

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
    public void addLibraryRoot() throws FileNotFoundException {
        var libraryRootPath = RESOURCEDIR + "/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);
        var expectedDevice = gson.fromJson(new FileReader(RESOURCEDIR + "/audio/jst2018-12-09.json"), FileSystemAudioContentDevice.class);

        assertTrue(expectedDevice.getLibraryRoots().values().containsAll(device.getLibraryRoots().values()));
        assertEquals(1, device.getStorageDevices().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices()).get(0);
        var libraryRootFile = new File(libraryRootPath);
        assertTrue(storageDevice.getCapacity() >= libraryRootFile.getFreeSpace());
        assertTrue(storageDevice.getCapacity() < libraryRootFile.getTotalSpace());
        assertEquals(expectedDevice.getContent(), device.getContent());
    }

    @Test
    public void addMultipleLibraryRoot() throws FileNotFoundException {
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
        var deserialized = gson.fromJson(gson.toJson(device), FileSystemAudioContentDevice.class);

        assertEquals(device, deserialized);
    }

    @Test
    public void testMoveToDevice() {
        device.addLibraryRoot(TMPDIR);
        var testFile = RESOURCEDIR + "/audio/jst2018-12-09/jst2018-12-09t01.flac";
        var testSubLibPath = testFile.substring(StringUtils.lastOrdinalIndexOf(testFile, "/", 2));
        var contentData = new AudioContent();
        contentData.setLibraryPath(testSubLibPath);

        try (var isfs = Files.newInputStream(Paths.get(testFile))) {
            var content = device.addContentToDevice(isfs, contentData,
                    device.getLibraryRoots().keySet().iterator().next());
            assertTrue(device.getContent().containsKey(content.getId()));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}