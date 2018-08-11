package org.meltzg.jmlm.device;

import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;
import org.meltzg.jmlm.device.content.FSAudioContentNode;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an abstract music device on a filesystem.
 *
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class FSAudioContentDevice extends AbstractContentDevice {

    public FSAudioContentDevice() {
        super();
        this.content = new ContentRootWrapper(new FSAudioContentNode());
    }

    @Override
    public boolean addLibraryRoot(String id) {
        String addId = id;
        // since this device is an abstraction ontop of the filesystem,
        // content must be read when adding the library root instead of
        // at device initialization
        if (!content.contains(id)) {
            AbstractContentNode node = readDeviceContent(id);
            content.addToRoot(node);
            content.refreshRootInfo();

            addId = node.getId();
        }

        return super.addLibraryRoot(addId);
    }

    @Override
    protected List<String> getChildIds(String pId) {
        List<String> childIds = new ArrayList<>();
        File[] children = (new File(pId)).listFiles();
        if (children != null) {
            for (File child : children) {
                childIds.add(child.getAbsolutePath());
            }
        }

        return childIds;
    }

    @Override
    protected AbstractContentNode createDirNode(String pId, String name) {
        try {
            pId = validateId(pId);
            String fullPath = pId + '/' + name;
            File folder = new File(fullPath);
            if (folder.mkdir()) {
                return readNode(fullPath);
            }
        } catch (InvalidContentIDException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected AbstractContentNode createContentNode(String pId, File file) {
        try {
            pId = validateId(pId);
            AbstractContentNode parent = content.getNode(pId);
            if (parent.isDir() && file.exists() && !file.isDirectory()) {
                Path destination = Paths.get(pId, file.getName());
                Files.copy(file.toPath(), destination);
                return readNode(destination.toFile().getAbsolutePath());
            } else {
                System.err.println("Cannot create node under " + pId + ": it is not a directory");
            }
        } catch (InvalidContentIDException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected AbstractContentNode copyNode(String pId, String id, String tmpFolder) {
        try {
            pId = validateId(pId);
            id = validateId(id);
            AbstractContentNode parent = content.getNode(pId);
            AbstractContentNode toMove = content.getNode(id);
            if (parent.isDir()) {
                Path finalPath = Paths.get(parent.getId(), toMove.getOrigName());
                Files.copy(Paths.get(toMove.getId()), finalPath);
                return readNode(finalPath.toFile().getAbsolutePath());
            } else {
                System.err.println("Cannot create node under " + pId + ": it is not a directory");
            }
        } catch (InvalidContentIDException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected AbstractContentNode readNode(String id) {
        FSAudioContentNode node = null;
        File toRead = new File(id);
        if (!toRead.exists()) {
            System.err.println("Node with ID " + id + " could not be read. File does not exist");
        } else {
            node = new FSAudioContentNode(id);
            if (!node.isValid()) {
                node = null;
            }
        }
        return node;
    }

    @Override
    protected boolean deleteNode(String id) {
        try {
            id = validateId(id);
            Files.delete(Paths.get(id));
            return true;
        } catch (InvalidContentIDException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean retrieveNode(String id, String destFolder) {
        try {
            id = validateId(id);
            AbstractContentNode node = content.getNode(id);
            File dir = new File(destFolder);
            if (!dir.exists()) {
                System.err.println(destFolder + " does not exist");
            } else if (!dir.isDirectory()) {
                System.err.println(destFolder + " is not a directory");
            } else {
                Files.copy(Paths.get(node.getId()), Paths.get(destFolder, node.getOrigName()), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (InvalidContentIDException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected StorageDevice getStorageDevice(String id) {
        StorageDevice device = new StorageDevice();
        Path idPath = Paths.get(id);
        File idFile = new File(idPath.toString());
        BigInteger capacity = content.getNode(AbstractContentNode.ROOT_ID).getTotalSize();
        capacity.add(BigInteger.valueOf(idFile.getUsableSpace()));

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            device.setId(idPath.getRoot().toString());
        } else {
            try {
                BasicFileAttributes fileAttrs = Files.readAttributes(Paths.get(id), BasicFileAttributes.class);
                Object fileKey = fileAttrs.fileKey();
                System.out.println(fileAttrs.getClass().getCanonicalName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        device.setCapacity(capacity);
        return device;
    }

    @Override
    protected String validateId(String id) throws InvalidContentIDException {
        id = Paths.get(id).toAbsolutePath().toString();
        return super.validateId(id);
    }

}