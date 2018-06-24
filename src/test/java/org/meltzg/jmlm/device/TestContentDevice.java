package org.meltzg.jmlm.device;

import com.google.gson.*;
import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;
import org.meltzg.jmlm.device.content.TestContentNode;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestContentDevice extends AbstractContentDevice {

    private JsonObject rawContent;

    public TestContentDevice(String testFilePath) throws FileNotFoundException {
        super();
        Gson gson = new Gson();
        this.rawContent = gson.fromJson(new FileReader(testFilePath), JsonObject.class);
        this.content = new ContentRootWrapper(readDeviceContent(AbstractContentNode.ROOT_ID));
    }

    @Override
    protected List<String> getChildIds(String pId) {
        List<String> ids = new ArrayList<>();
        JsonObject node = this.rawContent.getAsJsonObject(pId);
        JsonArray children = node.getAsJsonArray("children");

        for (JsonElement id : children) {
            ids.add(id.getAsString());
        }

        return ids;
    }

    @Override
    protected AbstractContentNode createDirNode(String pId, String name) {
        try {
            this.validateId(pId);
            return new TestContentNode(UUID.randomUUID().toString(),
                    pId,
                    name,
                    true,
                    BigInteger.ZERO,
                    BigInteger.ZERO);
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected AbstractContentNode createContentNode(String pId, File file) {
        try {
            this.validateId(pId);
            Gson gson = new Gson();
            AbstractContentNode node = gson.fromJson(new FileReader(file), TestContentNode.class);
            node.setPId(pId);
            return node;
        } catch (FileNotFoundException | InvalidContentIDException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected AbstractContentNode readNode(String id) {
        JsonObject jNode = this.rawContent.getAsJsonObject(id);
        if (jNode == null) {
            return null;
        }

        JsonPrimitive jPId = jNode.getAsJsonPrimitive("pId");

        String pId = jPId != null ? jPId.getAsString() : null;
        String origName = jNode.getAsJsonPrimitive("origName").getAsString();
        boolean isDir = jNode.getAsJsonPrimitive("isDir").getAsBoolean();
        BigInteger size = new BigInteger(jNode.getAsJsonPrimitive("size").getAsString());
        BigInteger capacity = new BigInteger(jNode.getAsJsonPrimitive("capacity").getAsString());

        TestContentNode node = new TestContentNode(
                id,
                pId,
                origName,
                isDir,
                size,
                capacity);

        return node;
    }

    @Override
    protected AbstractContentNode copyNode(String pId, String id, String tmpFolder) {
        try {
            pId = validateId(pId);
            id = validateId(id);

            AbstractContentNode parent = content.getNode(pId);
            AbstractContentNode toMove = content.getNode(id);
            if (parent.isDir()) {
                return new TestContentNode(toMove);
            } else {
                System.err.println("Cannot create node under " + pId + ": it is not a directory");
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected boolean deleteNode(String id) {
        return true;
    }

    @Override
    protected boolean retrieveNode(String id, String destFolder) {
        try {
            validateId(id);
            AbstractContentNode node = new TestContentNode(this.content.getNode(id));
            Gson gson = new Gson();
            gson.toJson(node, new FileWriter(String.format("%s/%s.json", destFolder, node.getOrigName())));
            return true;
        } catch (InvalidContentIDException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
