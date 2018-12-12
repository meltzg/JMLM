package org.meltzg.jmlm.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.Assert.*;

public class FileSystemAudioContentDeviceTest {

    private static Gson gson;

    @BeforeClass
    public static void setup() {
        gson = new GsonBuilder()
                .registerTypeAdapter(FileSystemAudioContentDevice.class,
                        new FileSystemAudioContentDevice())
                .create();
    }

    @Test
    public void addLibraryRoot() throws FileNotFoundException {
        var device = new FileSystemAudioContentDevice();
        device.addLibraryRoot("./src/test/resources/audio/jst2018-12-09");
        Collection storageDevices = device.getStorageDevices();
        var expectedDevice = gson.fromJson(new FileReader("./src/test/resources/audio/jst2018-12-09.json"), FileSystemAudioContentDevice.class);

        assertEquals(expectedDevice.getLibraryRoots(), device.getLibraryRoots());
        assertEquals(expectedDevice.getStorageDevices().size(), device.getStorageDevices().size());
    }
}