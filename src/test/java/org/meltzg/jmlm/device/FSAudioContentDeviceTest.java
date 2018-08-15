package org.meltzg.jmlm.device;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.meltzg.TestConfig;
import org.meltzg.jmlm.device.content.AbstractContentNode;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
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

    @Override
    protected BigInteger getLibCapacity(AbstractContentDevice device, AbstractContentNode node) {
        return BigInteger.valueOf(new File(node.getId()).getUsableSpace()).add(node.getTotalSize());
    }
}