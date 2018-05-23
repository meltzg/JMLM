package org.meltzg.jmlm.device.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class TestContentNode extends AbstractContentNode {
    public TestContentNode() {
        super();
    }
    public TestContentNode(String id, String pId, String origName, boolean isDir, BigInteger size, BigInteger capacity) {
        super(id, pId, origName, isDir, size, capacity);
    }

    public TestContentNode(AbstractContentNode other) {
        this(other.id, other.pId, other.origName, other.isDir, other.size, other.capacity);
    }

    @Override
    protected AbstractContentNode getInstance() {
        return new TestContentNode();
    }

    public static void main(String[] args) {
        TestContentNode root = new TestContentNode(AbstractContentNode.ROOT_ID,
                null,
                "root",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode stor1 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "stor1",
                true,
                BigInteger.ZERO,
                BigInteger.valueOf(512));

        TestContentNode stor2 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "stor2",
                true,
                BigInteger.ZERO,
                BigInteger.valueOf(512));

        TestContentNode dir1 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "dir1",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir2 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "dir2",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir3 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "dir3",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode dir4 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "dir4",
                true,
                BigInteger.ZERO,
                BigInteger.ZERO);

        TestContentNode file1 = new TestContentNode(UUID.randomUUID().toString(),
                null,
                "file1",
                false,
                BigInteger.valueOf(256),
                BigInteger.ZERO);

        TestContentNode file2 = new TestContentNode(UUID.randomUUID().toString(),
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

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(root.getClass(), root);
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        System.out.println(gson.toJson(root));
        AbstractContentNode deserialized = gson.fromJson(gson.toJson(root), TestContentNode.class);
    }
}
