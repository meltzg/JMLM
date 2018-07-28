package org.meltzg.jmlm.device;

import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

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
    /**
     * A device may contain several directories that make up the overall library of the device
     */
    protected Set<String> libRoots;

    public AbstractContentDevice() {
        this.deviceId = UUID.randomUUID().toString();
        this.libRoots = new HashSet<>();
    }

    /**
     * Adds a node to this device's library root list.
     *
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

            assignLibCapacities();

            return true;
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes an ID as a library root
     *
     * @param libRoot the ID of the library root to remove
     * @return success of removal
     */
    public boolean removeLibRoot(String libRoot) {
        boolean success = libRoots.remove(libRoot);
        assignLibCapacities();
        return success;

    }

    /**
     * @return the set of library roots
     */
    public Set<String> getLibRoots() {
        return libRoots;
    }

    /**
     * Transfers a file to the device.  The returned subtree can have a null representing the
     * content that was attempted to transfer if transfer was unsuccessful
     *
     * @param filepath the path to the file to transfer
     * @param destId   the ID of the content node to transfer the file under
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
     *
     * @param id         the ID of the content to transfer
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
     *
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanDevice();
        }
        return success;
    }

    /**
     * Moves content from one location on the device to another. DOES NOT WORK ON DIRECTORIES
     *
     * @param id             the id of the content to move
     * @param destId         the ID of the content node to transfer the file under
     * @param destFolderPath the path under destId to transfer the file under (non existant directories will be created)
     * @param tmpFolder      a temporary file that can be used as an intermediary (not all devices support a direct move)
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
            fPair.lastNode.addChild(movedNode);
            if (highestCreatNode == null && movedNode != null) {
                highestCreatNode = movedNode;
            }
            if (movedNode != null) {
                removeFromDevice(id);
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        } finally {
            cleanDevice();
        }
        return highestCreatNode;
    }

    /**
     * Reads the device's content from the given ID and returns a tree with ID as the root
     *
     * @param id ID to start the device read on
     * @return AbstractContentNode tree with ID as the root
     */
    public AbstractContentNode readDeviceContent(String id) {
        AbstractContentNode root = readNode(id);

        if (root != null) {
            Queue<AbstractContentNode> nodeQueue = new LinkedList<>();
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
     * Removes all empty directories from the device and refreshes the content root's metadata
     */
    public void cleanDevice() {
        Stack<AbstractContentNode> stack = new Stack<>();
        Set<String> visited = new HashSet<>();
        stack.add(content.getNode(AbstractContentNode.ROOT_ID));

        while (!stack.empty()) {
            if (!visited.contains(stack.peek().getId()) && stack.peek().getChildren().size() > 0) {
                visited.add(stack.peek().getId());
                for (AbstractContentNode child : stack.peek().getChildren()) {
                    stack.add(child);
                }
            } else {
                AbstractContentNode node = stack.pop();
                if (node.isDir() && node.getChildren().size() == 0 && !libRoots.contains(node.getId())) {
                    if (deleteNode(node.getId())) {
                        content.getNode(node.getPId()).removeChild(node.getId());
                    }
                }
            }
        }

        content.refreshRootInfo();
    }

    /**
     * Creates a map of the non dir content keyed by their path from their library root
     *
     * @return Map<path from lib to node , node>
     */
    public Map<String, AbstractContentNode> getPathToContent() {
        Map<String, AbstractContentNode> pathToContent = new HashMap<>();

        Collection<String> roots;
        if (libRoots.size() != 0) {
            roots = libRoots;
        } else {
            roots = Arrays.asList(AbstractContentNode.ROOT_ID);
        }

        for (String root : roots) {
            Stack<AbstractContentNode> stack = new Stack<>();
            List<String> currPath = new ArrayList<>();
            stack.add(content.getNode(root));

            while (!stack.empty()) {
                AbstractContentNode node = stack.pop();
                currPath.add(node.getOrigName());
                if (!node.isDir()) {
                    pathToContent.put(String.join("/", currPath), node);
                    currPath.remove(currPath.size() - 1);
                }
                stack.addAll(node.getChildren());
            }
        }

        return pathToContent;
    }

    public AbstractContentNode getNode(String id) {
        return content.getNode(id);
    }

    /**
     * Validates a given ID.  A validated ID is returned. The returned ID may not be an exact match to the input.
     * This can happen if the device is able to correct the formatting of the ID.  For this reason, the returned
     * value should be used
     *
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
     * Assigns storage capacities to the Library root nodes.
     * Libraries that exist on the same storage device should each be given an equal share
     * <p>
     * This implementation assumes that storage devices on the device are represented as
     * directories with a capacity of > 0
     */
    protected void assignLibCapacities() {
        List<AbstractContentNode> storageDevices = new ArrayList<>();
        Map<String, List<AbstractContentNode>> storageDeviceMap = new HashMap<>();

        Stack<AbstractContentNode> stack = new Stack<>();

        // Find the storage devices
        stack.add(content.getNode(AbstractContentNode.ROOT_ID));
        while (!stack.empty()) {
            AbstractContentNode node = stack.pop();
            if (node.getCapacity().compareTo(BigInteger.ZERO) > 0) {
                storageDevices.add(node);
            } else {
                stack.addAll(node.getChildren());
            }
        }

        for (String libRoot : libRoots) {
            for (AbstractContentNode device : storageDevices) {
                ContentRootWrapper wrapper = new ContentRootWrapper(device);
                if (wrapper.contains(libRoot)) {
                    if (!storageDeviceMap.containsKey(device.getId())) {
                        storageDeviceMap.put(device.getId(), new ArrayList<>());
                    }
                    storageDeviceMap.get(device.getId()).add(content.getNode(libRoot));
                }
            }
        }

        for (Map.Entry<String, List<AbstractContentNode>> storageMapping : storageDeviceMap.entrySet()) {
            BigInteger cap = content.getNode(storageMapping.getKey()).getCapacity();
            cap = cap.divide(BigInteger.valueOf(storageMapping.getValue().size()));
            for (AbstractContentNode node : storageMapping.getValue()) {
                node.setCapacity(cap);
            }
        }
    }

    /**
     * Recursive helper function to delete device content.  Will likely be made iterative in a later version
     *
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
     *
     * @param id   the ID of the node to create a folder path under
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
     *
     * @param pId the ID of the node to retrieve child IDs from
     * @return a List of child IDs
     */
    protected abstract List<String> getChildIds(String pId);

    /**
     * Creates a new directory node on the device
     *
     * @param pId  the ID of the directory to create a new directory under
     * @param name the name to give the new directory
     * @return the newly created node or null if it was unsuccessful
     */
    protected abstract AbstractContentNode createDirNode(String pId, String name);

    /**
     * Creates a new content node on the device
     *
     * @param pId  the ID of the directory to move the content under
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
     *
     * @param pId       the ID of the content node to transfer the file under
     * @param id        the id of the content to move
     * @param tmpFolder a temporary file that can be used as an intermediary (not all devices support a direct move)
     * @return the moved content node
     */
    protected abstract AbstractContentNode copyNode(String pId, String id, String tmpFolder);

    /**
     * Removes content from the device by ID.  If the node with the given ID is a non-empty
     * directory, the deletion will fail
     *
     * @param id ID of the node to remove from the device
     * @return true if the removal was successful.
     */
    protected abstract boolean deleteNode(String id);

    /**
     * Transfers a file from the device
     *
     * @param id         the ID of the content to transfer
     * @param destFolder the path of the folder to transfer the content to
     * @return true if transfer is successful
     */
    protected abstract boolean retrieveNode(String id, String destFolder);

    /**
     * Represents the work done when creating a folder path.
     */
    private class FolderPair {
        /**
         * when creating folders, this is the highest folder created (null if none created)
         */
        public AbstractContentNode createdNode;
        /**
         * when creating folders this should be the ID of the last folder created/returned
         */
        public AbstractContentNode lastNode;
    }
}