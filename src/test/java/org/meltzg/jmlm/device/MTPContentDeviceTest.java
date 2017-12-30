package org.meltzg.jmlm.device;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.BeforeClass;
import org.meltzg.TestConfig;

public class MTPContentDeviceTest extends AbstractContentDeviceTest {
	private static String testDevId;
	private static String testDesc;
	private static String testManufacturer;
	private static String testFName;
	
    @BeforeClass
    public static void setupBeforeClass() throws FileNotFoundException, IOException {
        Properties props = TestConfig.getProps();
        testLib1 = props.getProperty("test.mtpcd.lib1");
        testLib2 = props.getProperty("test.mtpcd.lib2");
        testChildFolder = props.getProperty("test.mtpcd.child");

        testDevId = props.getProperty("test.mtpcd.id");
        testDesc = props.getProperty("test.mtpcd.desc");
        testManufacturer = props.getProperty("test.mtpcd.manu");
        testFName = props.getProperty("test.mtpcd.fname");

        
        testFile1 = props.getProperty("test.mtpcd.file1");
        testFile2 = props.getProperty("test.mtpcd.file2");
        testDevPath1 = props.getProperty("test.mtpcd.path1");
        testDevPath2 = props.getProperty("test.mtpcd.path2");
    }
}