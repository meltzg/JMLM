package org.meltzg.jmlm.device.content;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.BeforeClass;
import org.meltzg.TestConfig;

public class ContentRootWrapperTest {
    @BeforeClass
    public static void setUpBeforeClass() throws FileNotFoundException, IOException {
        Properties props = TestConfig.getProps();
    }
}