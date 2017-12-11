package org.meltzg.jmlm.device.content;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.BeforeClass;
import org.meltzg.TestConfig;

public class ContentRootWrapperTest {
    @BeforeClass
    public static void setUpBeforeClass() throws FileNotFoundException, IOException {
        Properties props = TestConfig.getProps();
        AbstractContentNode root = mock(AbstractContentNode.class);
    }
}