package org.meltzg.jmlm.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileSystemAudioContentDeviceTest {

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
    public void before() {
        this.device = new FileSystemAudioContentDevice();
    }

    @Test
    public void addLibraryRoot() throws FileNotFoundException {
        var libraryRootPath = "./src/test/resources/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);
        var expectedDevice = gson.fromJson(new FileReader("./src/test/resources/audio/jst2018-12-09.json"), FileSystemAudioContentDevice.class);

        assertEquals(expectedDevice.getLibraryRoots(), device.getLibraryRoots());
        assertEquals(1, device.getStorageDevices().size());

        var storageDevice = new ArrayList<>(device.getStorageDevices()).get(0);
        var libraryRootFile = new File(libraryRootPath);
        assertTrue(storageDevice.getCapacity() >= libraryRootFile.getFreeSpace());
        assertTrue(storageDevice.getCapacity() < libraryRootFile.getTotalSpace());
    }

    @Test
    public void addMultipleLibraryRoot() throws FileNotFoundException {
        String[] libraryRoots = {
                "./src/test/resources/audio/jst2018-12-09",
                "./src/test/resources/audio/kwgg2016-10-29"
        };

        for (var root : libraryRoots) {
            device.addLibraryRoot(root);
        }

        assertEquals(libraryRoots.length, device.getLibraryRoots().size());
        for (var root : device.getLibraryRoots()) {
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
        var libraryRootPath = "./src/test/resources/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(libraryRootPath);
        device.addLibraryRoot(parentLibraryPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChildOfLibraryRoot() {
        var libraryRootPath = "./src/test/resources/audio/jst2018-12-09";
        var parentLibraryPath = libraryRootPath.substring(0, libraryRootPath.lastIndexOf('/'));
        device.addLibraryRoot(parentLibraryPath);
        device.addLibraryRoot(libraryRootPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNotDirectoryLibraryRoot() {
        device.addLibraryRoot("./src/test/resources/audio/jst2018-12-09.json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNotExistLibraryRoot() {
        device.addLibraryRoot("./src/test/resources/audio/NotFound");
    }

    @Test
    public void testSerialization() {
        var libraryRootPath = "./src/test/resources/audio/jst2018-12-09";
        device.addLibraryRoot(libraryRootPath);
        var deserialized = gson.fromJson(gson.toJson(device), FileSystemAudioContentDevice.class);

        assertTrue(device.equals(deserialized));
    }
}