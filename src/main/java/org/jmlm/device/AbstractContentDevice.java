package org.jmlm.device;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import org.jmlm.device.content.AbstractContentNode;
import org.jmlm.device.content.ContentRootNode;

public abstract class AbstractContentDevice {
    protected String deviceId;
    protected ContentRootNode content;
    protected Set<String> libRoots;

    public boolean addLibraryRoot(String id) {
        try {
            validateId(id);
            if (content.getNode(id).isDir()) {
                libRoots.add(id);
            } else {
                System.err.println("Library root must be a directory: " + id);
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public AbstractContentNode transferToDevice(String filepath, String destId, String destpath) {
        AbstractContentNode highestCreated = null;

        try {
            validateId(destId);

            File toTransfer = new File(filepath);
            if (!toTransfer.exists()) {
                throw new FileNotFoundException(filepath);
            }
            if (toTransfer.isDirectory()) {
                throw new IllegalArgumentException("!!! Cannot transfer directory to device: " + filepath);
            }

            String[] pathParts = destpath.split("[/\\]");
            String fileName = toTransfer.getName();
            AbstractContentNode parent = content.getNode(destId);

            for (String part : pathParts) {
                AbstractContentNode existantNode = parent.getChildByOName(part);
                if (existantNode != null) {
                    if (!existantNode.isDir()) {
                        throw new IllegalArgumentException("Node " + part + " already exists and is not a directory");
                    }
                    parent = existantNode;
                } else {
                    AbstractContentNode newFolder = createDirNode(parent.getId(), part);
                    if (newFolder == null) {
                        parent = null;
                        break;
                    }
                    parent.getChildren().add(newFolder);
                    parent = newFolder;
                    if (highestCreated == null) {
                        highestCreated = newFolder;
                    }
                }
            }

            if (parent != null) {
                AbstractContentNode contentNode = parent.getChildByOName(fileName);
                if (contentNode == null) {
                    contentNode = createContentNode(parent.getId(), toTransfer);
                    parent.getChildren().add(contentNode);
                } else {
                    throw new IllegalArgumentException(
                            "Node with name " + fileName + " already exists at location " + destpath);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }

        return highestCreated;
    }

    public boolean transferFromDevice(String id, String destFolder) {
        boolean success = false;
        try {
            validateId(id);
            AbstractContentNode toTransfer = content.getNode(id);
            String fullpath = destFolder + "/" + toTransfer.getOrigName();

            (new File(destFolder)).mkdirs();
            
            success = retrieveNode(id, fullpath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    public boolean removeFromDevice(String id) {
        // TODO stub
        boolean success = false;
        try {
            validateId(id);
            Stack<AbstractContentNode> trash = new Stack<AbstractContentNode>();
            Queue<AbstractContentNode> queue = new LinkedList<AbstractContentNode>();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }
        return success;
    }

    public AbstractContentNode moveOnDevice(String id, String destId, String destFolderPath, String tmpFolder) {
        // TODO stub
        AbstractContentNode highestCreatNode = null;
        try {
            validateId(id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }
        return highestCreatNode;
    }

    protected void validateId(String id) {
        if (content == null) {
            throw new NullPointerException("Device content has not been initialized.");
        }
        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }
        if (content.contains(id)) {
            throw new NullPointerException("Device does not contain an object with ID: " + id);
        }
    }

    protected abstract List<String> getChildIds(String pId);

    protected abstract AbstractContentNode createDirNode(String pId, String name);

    protected abstract AbstractContentNode createContentNode(String pId, File file);

    protected abstract AbstractContentNode moveNode(String pId, String id, String tmpFolder);

    protected abstract boolean deleteNode(String id);

    protected abstract boolean retrieveNode(String id, String destFolder);
}