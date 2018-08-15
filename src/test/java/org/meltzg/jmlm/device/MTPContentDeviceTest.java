package org.meltzg.jmlm.device;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.meltzg.TestConfig;
import org.meltzg.jmlm.device.content.AbstractContentNode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MTPContentDeviceTest extends AbstractContentDeviceTest {
    private static String testDevId;
    private static String testDesc;
    private static String testManufacturer;
    private static String testFName;

    @BeforeClass
    public static void setupBeforeClass() throws IOException {
        Properties props = TestConfig.getProps();
        testLib1 = props.getProperty("test.mtpcd.lib1");
        testLib2 = props.getProperty("test.mtpcd.lib2");

        testDevId = props.getProperty("test.mtpcd.id");
        testDesc = props.getProperty("test.mtpcd.desc");
        testManufacturer = props.getProperty("test.mtpcd.manu");
        testFName = props.getProperty("test.mtpcd.fname");


        testFile1 = props.getProperty("test.mtpcd.file1");
        testFile2 = props.getProperty("test.mtpcd.file2");
        testDevPath1 = props.getProperty("test.mtpcd.path1");
        testDevPath2 = props.getProperty("test.mtpcd.path2");
    }

    @Test
    public void testGetDevices() {
        List<MTPContentDevice.MTPDeviceInfo> deviceInfo = MTPContentDevice.getDevicesInfo();
        MTPContentDevice.MTPDeviceInfo found = MTPContentDevice.getDeviceInfo(testDevId);
        MTPContentDevice.MTPDeviceInfo found2 = null;

        for (MTPContentDevice.MTPDeviceInfo info : deviceInfo) {
            if (info.deviceId.equals(testDevId)) {
                found2 = info;
                break;
            }
        }

        assertEquals("Device has correct ID", testDevId, found.deviceId);
        assertEquals("Device has correct description", testDesc, found.description);
        assertEquals("Device has correct manufacturer", testManufacturer, found.manufacturer);
        assertEquals("Device has correct friendly name", testFName, found.friendlyName);

        assertNotNull("Device found: ", found);
        assertEquals("Specific device is the same as list device", found, found2);
    }

    @Override
    protected AbstractContentDevice getNewDevice() {
        return new MTPContentDevice(testDevId);
    }

    @Override
    protected BigInteger getLibCapacity(AbstractContentDevice device, AbstractContentNode node) {
        return null;
    }
}