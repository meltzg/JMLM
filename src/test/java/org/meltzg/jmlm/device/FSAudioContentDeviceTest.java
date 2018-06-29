package org.meltzg.jmlm.device;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.meltzg.TestConfig;

import java.io.IOException;
import java.util.Properties;

public class FSAudioContentDeviceTest extends AbstractContentDeviceTest {
    @BeforeClass
    public static void setupBeforeClass() throws IOException {
        Properties props = TestConfig.getProps();
        testLib1 = props.getProperty("test.fsacd.lib1");
        testLib2 = props.getProperty("test.fsacd.lib2");
        testChildFolder = props.getProperty("test.fsacd.child");

        testFile1 = props.getProperty("test.fsacd.file1");
        testFile2 = props.getProperty("test.fsacd.file2");
        testDevPath1 = props.getProperty("test.fsacd.path1");
        testDevPath2 = props.getProperty("test.fsacd.path2");
    }

    @Override
    protected AbstractContentDevice getNewDevice() {
        // TODO Auto-generated method stub
        return new FSAudioContentDevice();
    }
}