package org.meltzg.jmlm.device;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.meltzg.jmlm.device.content.AbstractContentNode;

public abstract class AbstractContentDeviceTest {
    protected static String testLib1;
    protected static String testLib2;
    protected static String testChildFolder;
    protected static String testFile1;
    protected static String testFile2;
    protected static String testDevPath1;
    protected static String testDevPath2;

    protected AbstractContentDevice device;

    @Test
    public void testLibraryRoots() {
        boolean addRoot1 = device.addLibraryRoot(testLib1);
        boolean addRoot2 = device.addLibraryRoot(testLib2);
        boolean addDup = device.addLibraryRoot(testLib1);
        
        if (testChildFolder == null) {
        	testChildFolder = device.getChildIds(testLib1).get(0);
        }
        
        boolean addChild = device.addLibraryRoot(testChildFolder);

        assertTrue("Device should be able to add a library root: ", addRoot1);
        assertTrue("Device should be able to add another library root: ", addRoot2);
        assertFalse("Device should not be able to add a library twice: ", addDup);
        assertFalse("Device should not be able to add a child of a library as a library: ", addChild);
        assertEquals("Device should have 2 library roots: ", device.getLibRoots().size(), 2);

        Set<String> libRoots = new HashSet<String>(device.getLibRoots());
        for (String libRoot : libRoots) {
            device.removeLibRoot(libRoot);
        }

        assertEquals("Device should be able to remove library root references: ", device.getLibRoots().size(), 0);
    }

    @Test
    public void testCRUDOps() throws FileNotFoundException {
        boolean addRoot1 = device.addLibraryRoot(testLib1);
        boolean addRoot2 = device.addLibraryRoot(testLib2);
        
        AbstractContentNode node1 = device.transferToDevice(testFile1, testLib1, testDevPath1);
        AbstractContentNode node2 = device.transferToDevice(testFile2, testLib2, testDevPath2);

        assertNotNull("Should be able to transfer file to device: ", node1);
        assertNotNull("Should be able to transfer file to another folder on device: ", node2);

        String fId1, fId2;
        AbstractContentNode leaf1 = getLeaf(node1);
        AbstractContentNode leaf2 = getLeaf(node2);

        boolean transFrom1 = device.transferFromDevice(leaf1.getId(), "./tmp");
        
        assertTrue("Should be able to transfer file from device: ", transFrom1);

        AbstractContentNode move1 = device.moveOnDevice(leaf1.getId(), testLib2, testDevPath2, "./tmp");
        AbstractContentNode move2 = device.moveOnDevice(leaf2.getId(), testLib1, testDevPath1, "./tmp");

        assertNotNull("Should be able to move file on device: ", move1);
        assertNotNull("Should be able to move another file on device: ", move2);

        boolean remove1 = device.removeFromDevice(node1.getId());
        boolean remove2 = device.removeFromDevice(node2.getId());

        assertTrue("Should be able to remove test file 1", remove1);
        assertTrue("Should be able to remove test file 2", remove2);
    }

    private AbstractContentNode getLeaf(AbstractContentNode node) {
        while (!node.getChildren().isEmpty()) {
            node = node.getChildren().iterator().next();
        }

        return node;
    }
}