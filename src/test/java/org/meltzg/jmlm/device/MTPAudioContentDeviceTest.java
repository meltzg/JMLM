package org.meltzg.jmlm.device;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.meltzg.jmlm.CommonUtil.TMPDIR;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class MTPAudioContentDeviceTest extends FileSystemAudioContentDeviceTest {
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
    public void TestGetAllDeviceMountProperties() throws IOException {
        var allDevices = getDevice().getAllDeviceMountProperties();
        var expectedDevices = getDevice().toMap(new String[][]{
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

    MTPAudioContentDevice getDevice() {
        return (MTPAudioContentDevice) device;
    }
}