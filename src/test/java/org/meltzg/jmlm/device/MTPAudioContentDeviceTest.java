package org.meltzg.jmlm.device;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.device.storage.StorageDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.meltzg.jmlm.CommonUtil.TMPDIR;
import static org.meltzg.jmlm.device.MTPAudioContentDevice.MountProperties.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class MTPAudioContentDeviceTest {

    @Autowired
    AudioContentRepository contentRepo;

    MTPAudioContentDevice device;

    @Before
    public void setUp() throws Exception {
        device = new MTPAudioContentDevice("Device Name", contentRepo);
        FileUtils.forceMkdir(Paths.get(TMPDIR).toFile());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(Paths.get(TMPDIR).toFile());
    }

    @Test
    public void testGetAllDeviceMountProperties() throws IOException {
        var allDevices = device.getAllDeviceMountProperties();
        var expectedDevice = new HashMap<String, String>();
        expectedDevice.put(DESCRIPTION.toString(), "AK100_II");
        expectedDevice.put(FRIENDLY_NAME.toString(), null);
        expectedDevice.put(MANUFACTURER.toString(), "IRIVER");
        expectedDevice.put(SERIAL.toString(), "F2000018D562F2A412B4");
        expectedDevice.put(DEVICE_ID.toString(), "16642:4497:F2000018D562F2A412B4");
        assertEquals(1, allDevices.size());
        var deviceProps = allDevices.get(0);
        for (var prop : expectedDevice.entrySet()) {
            assertEquals(deviceProps.get(prop.getKey()), prop.getValue());
        }
        assertTrue(deviceProps.containsKey(BUS_LOCATION.toString()));
        assertTrue(deviceProps.containsKey(DEV_NUM.toString()));
    }

    @Test
    public void testMountDevice() throws IOException {
        var allDevices = device.getAllDeviceMountProperties();
        var deviceProps = allDevices.get(0);
        var mountableDevice = new MTPAudioContentDevice("Device 2", contentRepo, deviceProps);

        mountableDevice.mount();

        var children = Paths.get(mountableDevice.getRootPath()).toFile().listFiles();
        assertEquals(2, children.length);

        mountableDevice.unmount();

        children = Paths.get(mountableDevice.getRootPath()).toFile().listFiles();
        assertEquals(0, children.length);
    }

    @Test
    public void testAlreadyMounted() throws IOException {
        var allDevices = device.getAllDeviceMountProperties();
        var deviceProps = allDevices.get(0);
        var mountableDevice = new MTPAudioContentDevice("Device 2", contentRepo, deviceProps);

        mountableDevice.mount();

        var children = Paths.get(mountableDevice.getRootPath()).toFile().listFiles();
        assertEquals(2, children.length);

        var thrown = false;
        try {
            mountableDevice.mount();
        } catch (IOException e) {
            thrown = true;
        } finally {
            mountableDevice.unmount();
        }

        assertTrue(thrown);
    }

    @Test(expected = IOException.class)
    public void testAlreadyUnmounted() throws IOException {
        var allDevices = device.getAllDeviceMountProperties();
        var deviceProps = allDevices.get(0);
        var mountableDevice = new MTPAudioContentDevice("Device 2", contentRepo, deviceProps);

        mountableDevice.mount();

        var children = Paths.get(mountableDevice.getRootPath()).toFile().listFiles();
        assertEquals(2, children.length);

        mountableDevice.unmount();
        mountableDevice.unmount();
    }

    @Test
    public void testGetStorageDevice() throws IOException, URISyntaxException {
        var allDevices = device.getAllDeviceMountProperties();
        var deviceProps = allDevices.get(0);
        var mountableDevice = new MTPAudioContentDevice("Device 2", contentRepo, deviceProps);

        mountableDevice.mount();

        StorageDevice storage = mountableDevice.getStorageDevice(Paths.get("/Internal storage/Music"));

        assertNotNull(storage.getStorageId());
        assertFalse(storage.getStorageId().isEmpty());
    }
}