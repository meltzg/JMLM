package org.meltzg.jmlm.device.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class AbstractContentNodeTest {

    @Test
    public void testReadContentNode() {

    }

    @Test
    public void testReadDirNode() {

    }

    @Test
    public void testInvalidContentNode() {

    }

    @Test
    public void testContentNotFound() {

    }

    @Test
    public void testJsonSerialization() {
        AbstractContentNode root = getTestTree();

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(root.getClass(), root);
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        AbstractContentNode deserialized = gson.fromJson(gson.toJson(root), TestContentNode.class);

        assertEquals(root, deserialized);
    }

    @Test
    public void testNodeEquality() {
        TestContentNode tree1 = getTestTree();

        TestContentNode tree2 = getTestTree();

        assertEquals(tree1, tree2);
        assertNotEquals(tree1, null);

        tree2.addChild(new TestContentNode());
        assertNotEquals(tree1, tree2);
    }

    @Test
    public void testAddChild() {
        TestContentNode root = new TestContentNode(AbstractContentNode.ROOT_ID,
                null,
                "root",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode file1 = new TestContentNode("1",
                null,
                "file1",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        assertTrue(root.addChild(file1));
        assertFalse(root.addChild(file1));
        assertFalse(root.addChild(null));

        AbstractContentNode child = root.getChild(file1.getId());
        assertEquals(file1, child);
    }

    @Test
    public void testRemoveChild() {
        TestContentNode root = new TestContentNode(AbstractContentNode.ROOT_ID,
                null,
                "root",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode file1 = new TestContentNode("1",
                null,
                "file1",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        assertTrue(root.addChild(file1));
        assertFalse(root.removeChild("asdf"));
        assertFalse(root.removeChild(null));
        assertTrue(root.removeChild(file1.getId()));
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testGetChildByName() {
        TestContentNode root = new TestContentNode(AbstractContentNode.ROOT_ID,
                null,
                "root",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode file1 = new TestContentNode("1",
                null,
                "file1",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        assertTrue(root.addChild(file1));
        assertEquals(file1, root.getChildByOName(file1.getOrigName()));
        assertNull(root.getChildByOName("asdf"));
        assertNull(root.getChildByOName(null));
    }

    @Test
    public void testGetTotalSize() {
        assertEquals(BigInteger.valueOf(512), getTestTree().getTotalSize());
    }

    private TestContentNode getTestTree() {
        TestContentNode root = new TestContentNode(AbstractContentNode.ROOT_ID,
                null,
                "root",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode stor1 = new TestContentNode("1",
                null,
                "stor1",
                true,
                BigInteger.ZERO,
                BigInteger.valueOf(512));

        TestContentNode stor2 = new TestContentNode("2",
                null,
                "stor2",
                true,
                BigInteger.ZERO,
                BigInteger.valueOf(512));

        TestContentNode dir1 = new TestContentNode("3",
                null,
                "dir1",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir2 = new TestContentNode("4",
                null,
                "dir2",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir3 = new TestContentNode("5",
                null,
                "dir3",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir4 = new TestContentNode("6",
                null,
                "dir4",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode file1 = new TestContentNode("7",
                null,
                "file1",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        TestContentNode file2 = new TestContentNode("8",
                null,
                "file2",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        dir1.addChild(file1);
        dir3.addChild(file2);
        stor1.addChild(dir1);
        stor1.addChild(dir2);
        stor2.addChild(dir3);
        stor2.addChild(dir4);
        root.addChild(stor1);
        root.addChild(stor2);

        return root;
    }
}