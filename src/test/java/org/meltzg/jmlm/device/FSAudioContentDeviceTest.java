package org.meltzg.jmlm.device;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.meltzg.TestConfig;

public class FSAudioContentDeviceTest extends AbstractContentDeviceTest {
    @BeforeClass
    public static void setupBeforeClass() throws FileNotFoundException, IOException {
        Properties props = TestConfig.getProps();
        testLib1 = props.getProperty("test.fsacd.lib1");
        testLib2 = props.getProperty("test.fsacd.lib2");
        testChildFolder = props.getProperty("test.fsacd.child");
    }

    @Before
    public void beforeTests() {
        device = new FSAudioContentDevice();
    }
}