package org.meltzg.jmlm.device;

import com.google.gson.Gson;
import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;
import org.meltzg.jmlm.device.content.TestContentNode;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestContentDevice extends AbstractContentDevice {

    public TestContentDevice(String testFilePath) throws FileNotFoundException {
        super();
        Gson gson = new Gson();
        this.content = new ContentRootWrapper(gson.fromJson(new FileReader(testFilePath), TestContentNode.class));
    }

    @Override
    protected List<String> getChildIds(String pId) {
        List<String> ids = new ArrayList<>();
        AbstractContentNode parent = this.content.getNode(pId);
        for (AbstractContentNode child : parent.getChildren()) {
            ids.add(child.getId());
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
        return this.content.getNode(id);
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
