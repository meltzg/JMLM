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

/**
 * Represents a media content device.  Contains the common logic for manipulating a device's content.
 * Relies on the Template design pattern for device specific implementations.
 * 
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public abstract class AbstractContentDevice {
    protected String deviceId;
    protected ContentRootWrapper content;
    /** A device may contain several directories that make up the overall library of the device */
    protected Set<String> libRoots;

    public AbstractContentDevice() {
		this.deviceId = UUID.randomUUID().toString();
        this.libRoots = new HashSet<String>();
	}

    /**
     * Adds a node to this device's library root list.
     * @param id the ID to add as a library root
     * @return true if the ID was successfully added as a library root.  
     * false if the ID is contained within another library root, the ID is already a library root, 
     * no node with the given ID exists, or the ID is not for a directory
     */
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

    /**
     * Removes an ID as a library root
     * @param libRoot the ID of the library root to remove
     * @return success of removal
     */
    public boolean removeLibRoot(String libRoot) {
        return libRoots.remove(libRoot);
	}

    /** @return the set of library roots */
    public Set<String> getLibRoots() {
        return libRoots;
    }

    /**
     * Transfers a file to the device.  The returned subtree can have a null representing the 
     * content that was attempted to transfer if transfer was unsuccessful
     * @param filepath the path to the file to transfer
     * @param destId the ID of the content node to transfer the file under
     * @param destpath the path under destId to transfer the file under (non existant directories will be created)
     * @return a sub tree with the root being the highest new node created or null if nothing was created
     * @throws FileNotFoundException
     */
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

    /**
     * Transfers a file from the device
     * @param id the ID of the content to transfer
     * @param destFolder the path of the folder to transfer the content to
     * @return true if transfer is successful
     */
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

    /**
     * Removes content from the device by ID.  This will recursively remove content.
     * @param id ID of the node to remove from the device
     * @return true if the removal was successful.  A failed removal can be partial if node has children
     */
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
    
    /**
     * Moves content from one location on the device to another. DOES NOT WORK ON DIRECTORIES
     * @param id the id of the content to move
     * @param destId the ID of the content node to transfer the file under
     * @param destFolderPath the path under destId to transfer the file under (non existant directories will be created)
     * @param tmpFolder a temporary file that can be used as an intermediary (not all devices support a direct move)
     * @return a sub tree with the root being the highest new node created or null if nothing was created
     */
    public AbstractContentNode moveOnDevice(String id, String destId, String destFolderPath, String tmpFolder) {
        AbstractContentNode highestCreatNode = null;
        try {
            id = validateId(id);
            destId = validateId(destId);

            if (content.getNode(id).isDir()) {
                throw new InvalidContentIDException("Cannot move directories");
            }
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

    /**
     * Reads the device's content from the given ID and returns a tree with ID as the root
     * @param id ID to start the device read on
     * @return AbstractContentNode tree with ID as the root
     */
    public AbstractContentNode readDeviceContent(String id) {
        AbstractContentNode root = readNode(id);
        
        if (root != null) {
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
        }

        return root;
    }

    /**
     * Validates a given ID.  A validated ID is returned. The returned ID may not be an exact match to the input.  
     * This can happen if the device is able to correct the formatting of the ID.  For this reason, the returned 
     * value should be used
     * @param id the ID to validate
     * @return the validated ID
     */
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

    /**
     * Recursive helper function to delete device content.  Will likely be made iterative in a later version
     * @param id ID of the node to remove from the device
     * @return true if the removal was successful.  A failed removal can be partial if node has children
     */
    private boolean removeFromDeviceRecursive(String id) {
        boolean success = false;
        try {
            id = validateId(id);
            AbstractContentNode node = content.getNode(id);

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

    /**
     * Creates a directory node path
     * @param id the ID of the node to create a folder path under
     * @param path the desired folder path (creates all non-existant directories)
     * @return a FolderPair representing the work done
     */
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

    /**
     * Retrieves the child IDs of a given node from the device
     * @param pId the ID of the node to retrieve child IDs from
     * @return a List of child IDs
     */
    protected abstract List<String> getChildIds(String pId);

    /**
     * Creates a new directory node on the device
     * @param pId the ID of the directory to create a new directory under
     * @param name the name to give the new directory
     * @return the newly created node or null if it was unsuccessful
     */
    protected abstract AbstractContentNode createDirNode(String pId, String name);

    /**
     * Creates a new content node on the device
     * @param pId the ID of the directory to move the content under
     * @param file the file to transfer to the device
     * @return the newly created node or null if it was unsuccessful
     */
    protected abstract AbstractContentNode createContentNode(String pId, File file);

    /**
     * @param id The ID of the node to read
     * @return the node if found, null otherwise
     */
    protected abstract AbstractContentNode readNode(String id);

    /**
     * copies content from one location on the device to another. DOES NOT WORK ON DIRECTORIES
     * @param id the id of the content to move
     * @param pId the ID of the content node to transfer the file under
     * @param tmpFolder a temporary file that can be used as an intermediary (not all devices support a direct move)
     * @return the moved content node
     */
    protected abstract AbstractContentNode copyNode(String pId, String id, String tmpFolder);

    /**
     * Removes content from the device by ID.  If the node with the given ID is a non-empty 
     * directory, the deletion will fail
     * @param id ID of the node to remove from the device
     * @return true if the removal was successful.
     */
    protected abstract boolean deleteNode(String id);

    /**
     * Transfers a file from the device
     * @param id the ID of the content to transfer
     * @param destFolder the path of the folder to transfer the content to
     * @return true if transfer is successful
     */
    protected abstract boolean retrieveNode(String id, String destFolder);
    
    /**
     * Represents the work done when creating a folder path.
     */
    private class FolderPair {
        /** when creating folders, this is the highest folder created (null if none created) */
        public AbstractContentNode createdNode;
        /** when creating folders this should be the ID of the last folder created/returned */
		public AbstractContentNode lastNode;
	}
}