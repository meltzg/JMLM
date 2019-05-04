package org.meltzg.jmlm.device;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.meltzg.jmlm.CommonUtil.TMPDIR;

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
        var expectedDevices = device.toMap(new String[][]{
                {"productId", "0x1191"},
                {"vendorId", "0x4102"},
                {"product", "UNKNOWN"},
                {"vendor", "UNKNOWN"}
        });
        assertEquals(1, allDevices.size());
        var deviceProps = allDevices.get(0);
        for (var prop : expectedDevices.entrySet()) {
            assertEquals(deviceProps.get(prop.getKey()), prop.getValue());
        }
        assertTrue(deviceProps.containsKey("busLocation"));
        assertTrue(deviceProps.containsKey("devNum"));
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

    @Test
    public void testAlreadyUnmounted() throws IOException {
        var allDevices = device.getAllDeviceMountProperties();
        var deviceProps = allDevices.get(0);
        var mountableDevice = new MTPAudioContentDevice("Device 2", contentRepo, deviceProps);

        mountableDevice.mount();

        var children = Paths.get(mountableDevice.getRootPath()).toFile().listFiles();
        assertEquals(2, children.length);

        mountableDevice.unmount();

        var thrown = false;
        try {
            mountableDevice.unmount();
        } catch (IOException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }
}