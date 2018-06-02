package org.meltzg.jmlm.device.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meltzg.TestConfig;
import org.meltzg.jmlm.device.InvalidContentIDException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import static org.junit.Assert.*;

public class ContentRootWrapperTest {

    private ContentRootWrapper root;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Properties props = TestConfig.getProps();
    }

    @Before
    public void setUp() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TestContentNode.class, new TestContentNode());
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();

        FileReader reader = null;
        try {
            reader = new FileReader("src/test/resources/test-device.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AbstractContentNode rawRoot = gson.fromJson(reader, TestContentNode.class);
        root = new ContentRootWrapper(rawRoot);
    }

    @Test
    public void testContainsContent() {
        assertTrue(root.contains("DEVICE"));
        for (Integer i = 1; i <= 8; i++) {
            assertTrue(root.contains(i.toString()));
        }
        assertFalse(root.contains("NotValid"));
    }

    @Test
    public void testGetNode() {
        AbstractContentNode node = root.getNode("3");
        assertEquals("3", node.getId());
        assertEquals("1", node.getPId());
        assertEquals("dir1", node.getOrigName());
        assertEquals(1, node.getChildren().size());

        assertNull(root.getNode("NotValid"));
    }

    @Test
    public void testAddToRoot() {
        AbstractContentNode subRoot = new TestContentNode(
                "9",
                null,
                "newDir",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        AbstractContentNode subNode = new TestContentNode(
                "10",
                null,
                "newNode",
                true,
                BigInteger.valueOf(512),
                BigInteger.ZERO);

        subRoot.addChild(subNode);

        assertTrue(root.addToRoot(subRoot));
        assertFalse(root.addToRoot(subRoot));

        testContainsContent();
        assertTrue(root.contains(subRoot.getId()));
        assertEquals(AbstractContentNode.ROOT_ID, subRoot.getPId());
        assertTrue(root.contains(subNode.getId()));
    }

    @Test
    public void testRefreshRootInfo() {
        AbstractContentNode subRoot = new TestContentNode(
                "9",
                null,
                "newDir",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        root.getNode("3").addChild(subRoot);
        try {
            root.refreshRootInfo(subRoot);
            assertTrue(root.contains(subRoot.getId()));
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
            fail();
        }

        try {
            root.refreshRootInfo(subRoot);
            fail();
        } catch (InvalidContentIDException e) {
            assertTrue(true);
        }
    }
}