package org.meltzg.jmlm.device;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;

public abstract class AbstractContentDevice {
    protected String deviceId;
    protected ContentRootWrapper content;
    protected Set<String> libRoots;

    public AbstractContentDevice() {
		this.deviceId = UUID.randomUUID().toString();
        this.libRoots = new HashSet<String>();
	}

    public boolean addLibraryRoot(String id) {
        try {
            id = validateId(id);
            AbstractContentNode node = content.getNode(id);
            if (node.isDir()) {
                for (String libRoot : libRoots) {
                    ContentRootWrapper tmpRoot = new ContentRootWrapper(content.getNode(libRoot));
                    if (tmpRoot.contains(id)) {
                        System.err.println(id + " is a child of another library root");
                        return false;
                    }
                }
                if (!libRoots.add(id)) {                 
                    System.err.println(id + " is already a library root");
                    return false;
                }
            } else {
                System.err.println("Library root must be a directory: " + id);
                return false;
            }

            return true;
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeLibRoot(String libRoot) {
        return libRoots.remove(libRoot);
	}

    public Set<String> getLibRoots() {
        return libRoots;
    }

    public AbstractContentNode transferToDevice(String filepath, String destId, String destpath) throws FileNotFoundException {
        AbstractContentNode highestCreated = null;

        try {
            destId = validateId(destId);

            File toTransfer = new File(filepath);
            if (!toTransfer.exists()) {
                throw new FileNotFoundException(filepath);
            }
            if (toTransfer.isDirectory()) {
                throw new IllegalArgumentException("!!! Cannot transfer directory to device: " + filepath);
            }

            
            String fileName = toTransfer.getName();
            AbstractContentNode parent = content.getNode(destId);

            FolderPair fPair = createFolderPath(destId, destpath);
            highestCreated = fPair.createdNode;
            parent = fPair.lastNode;

            if (parent != null) {
                AbstractContentNode contentNode = parent.getChildByOName(fileName);
                if (contentNode == null) {
                    contentNode = createContentNode(parent.getId(), toTransfer);
                    parent.addChild(contentNode);
                    if (highestCreated == null) {
                        highestCreated = contentNode;
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Node with name " + fileName + " already exists at location " + destpath);
                }
            }

        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }

        return highestCreated;
    }

    public boolean transferFromDevice(String id, String destFolder) {
        boolean success = false;
        try {
            id = validateId(id);

            (new File(destFolder)).mkdirs();
            
            success = retrieveNode(id, destFolder);
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        }

        return success;
    }

    public boolean removeFromDevice(String id) {
        boolean success = false;
        try {
            success = removeFromDeviceRecursive(id);
            if (success) {
                AbstractContentNode parent = content.getNode(content.getNode(id).getPId());
                success = parent.removeChild(id);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }
        return success;
    }
    
    public AbstractContentNode moveOnDevice(String id, String destId, String destFolderPath, String tmpFolder) {
        AbstractContentNode highestCreatNode = null;
        try {
            id = validateId(id);
            destId = validateId(destId);

            FolderPair fPair = createFolderPath(destId, destFolderPath);
            highestCreatNode = fPair.createdNode;
            AbstractContentNode movedNode = copyNode(fPair.lastNode.getId(), id, tmpFolder);
            if (highestCreatNode == null && movedNode != null) {
                highestCreatNode = movedNode;
                fPair.lastNode.addChild(movedNode);
            }
            if (movedNode != null) {
                removeFromDevice(id);
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }
        return highestCreatNode;
    }

    public AbstractContentNode readDeviceContent(String id) {
        AbstractContentNode root = readNode(id);
        
        Queue<AbstractContentNode> nodeQueue = new LinkedList<AbstractContentNode>();
        nodeQueue.add(root);
        while (!nodeQueue.isEmpty()) {
            AbstractContentNode node = nodeQueue.poll();
            List<String> childIds = getChildIds(node.getId());
            for (String cId : childIds) {
                AbstractContentNode cNode = readNode(cId);
                if (cNode != null) {
                    node.addChild(cNode);
                    nodeQueue.add(cNode);
                }
            }
        }

        return root;
    }

    protected String validateId(String id) throws InvalidContentIDException {
        if (content == null) {
            throw new InvalidContentIDException("Device content has not been initialized.");
        }
        if (id == null) {
            throw new InvalidContentIDException("ID cannot be null");
        }
        if (!content.contains(id)) {
            throw new InvalidContentIDException("Device does not contain an object with ID: " + id);
        }

        return id;
    }

    private boolean removeFromDeviceRecursive(String id) {
        boolean success = false;
        try {
            id = validateId(id);
            AbstractContentNode node = content.getNode(id);
            // for (int i = 0; i < node.getChildren().size();) {
            //     boolean childRemoved = removeFromDeviceRecursive(node.getChildren().get(i).getId());
            //     success &= childRemoved;
            //     if (childRemoved) {
            //         node.getChildren().remove(i);
            //     } else {
            //         i++;
            //     }
            // }

            for (AbstractContentNode child : node.getChildren()) {
                boolean childRemoved = removeFromDeviceRecursive(child.getId());
                if (childRemoved) {
                    node.removeChild(child.getId());
                }
            }

            if (node.getChildren().isEmpty()) {
                success = deleteNode(id);
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        }
        return success;
    }

    private FolderPair createFolderPath(String id, String path) {
        FolderPair pair = new FolderPair();
        try {
            id = validateId(id);
            
            AbstractContentNode parent = content.getNode(id);
            String[] pathParts = path.replaceFirst("^[/\\\\]", "").split("[/\\\\]");
            for (String part : pathParts) {
                AbstractContentNode existantNode = parent.getChildByOName(part);
                if (existantNode != null) {
                    if (!existantNode.isDir()) {
                        throw new IllegalArgumentException("Node " + part + " already exists and is not a directory");
                    }
                    parent = existantNode;
                    pair.lastNode = existantNode;
                } else {
                    AbstractContentNode newFolder = createDirNode(parent.getId(), part);
                    if (newFolder == null) {
                        parent = null;
                        pair.lastNode = null;
                        break;
                    }
                    parent.addChild(newFolder);
                    parent = newFolder;
                    content.refreshRootInfo(newFolder);
                    if (pair.createdNode == null) {
                        pair.createdNode = newFolder;
                    }
                    pair.lastNode = newFolder;
                }
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        } finally {
            content.refreshRootInfo();
        }

        return pair;
    }

    protected abstract List<String> getChildIds(String pId);

    protected abstract AbstractContentNode createDirNode(String pId, String name);

    protected abstract AbstractContentNode createContentNode(String pId, File file);

    protected abstract AbstractContentNode readNode(String id);

    protected abstract AbstractContentNode copyNode(String pId, String id, String tmpFolder);

    protected abstract boolean deleteNode(String id);

    protected abstract boolean retrieveNode(String id, String destFolder);
    
    private class FolderPair {
		public AbstractContentNode createdNode; // when creating folders, this is the highest folder created (null if none created)
		public AbstractContentNode lastNode;	// when creating folders this should be the ID of the last folder created/returned
	}
}