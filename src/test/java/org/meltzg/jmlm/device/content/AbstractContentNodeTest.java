package org.meltzg.jmlm.device.content;

import org.junit.Test;

public abstract class AbstractContentNodeTest {

    @Test
    public abstract void testReadContentNode();
    @Test
    public abstract void testReadDirNode();
    @Test
    public abstract void testInvalidContentNode();
    @Test
    public abstract void testContentNotFound();
}