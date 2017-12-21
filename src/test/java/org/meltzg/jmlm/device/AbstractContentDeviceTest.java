package org.meltzg.jmlm.device;

import static org.junit.Assert.*;

import org.junit.Test;

public abstract class AbstractContentDeviceTest {
    protected static String testLib1;
    protected static String testLib2;
    protected static String testChildFolder;

    protected AbstractContentDevice device;

    @Test
    public void testLibraryRoots() {
        boolean addRoot1 = device.addLibraryRoot(testLib1);
        boolean addRoot2 = device.addLibraryRoot(testLib2);
        boolean addDup = device.addLibraryRoot(testLib1);
        boolean addChild = device.addLibraryRoot(testChildFolder);

        assertTrue("Device should be able to add a library root: ", addRoot1);
        assertTrue("Device should be able to add another library root: ", addRoot2);
        assertFalse("Device should not be able to add a library twice: ", addDup);
        assertFalse("Device should not be able to add a child of a library as a library: ", addChild);
        assertEquals("Device should have 2 library roots: ", device.getLibRoots().size(), 2);
    }
}