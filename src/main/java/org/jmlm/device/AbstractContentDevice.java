package org.jmlm.device;

import java.util.Set;

import org.jmlm.device.access.IContentInterface;
import org.jmlm.device.content.AbstractContentNode;
import org.jmlm.device.content.ContentRootNode;

public abstract class AbstractContentDevice implements IContentInterface {
    protected String deviceId;
    protected ContentRootNode content;
    protected Set<String> libRoots;

    public boolean addLibraryRoot(String id) {
        if (content.contains(id)) {
            if (content.getNode(id).isDir()) {
                libRoots.add(id);
            } else {
                System.err.println("Library root must be a directory: " + id);
                return false;
            }
        } else {
            System.err.println("Device does not contain a node with ID " + id);
            return false;
        }

        return true;
    }

    public boolean cleanEmptyDirs(String id) {
        if (!content.contains(id)) {
            System.err.println("Device does not contain a node with ID " + id);
            return false;
        }

        AbstractContentNode node = content.getNode(id);

        for (int i = 0; i < node.getChildren().size();) {
            AbstractContentNode child = node.getChildren().get(i);
            boolean nodeRemoved = false;

            if (child.isDir()) {
                boolean ret = cleanEmptyDirs(child.getId());
                if (!ret) {
                    return false;
                }

                if (child.getChildren().isEmpty()) {
                    if (removeFromDevice(child.getId())) {
                        nodeRemoved = true;
                        node.getChildren().remove(i);
                    } else {
                        return false;
                    }
                }
            }

            if (!nodeRemoved) {
                i++;
            }
        }

        return true;
    }
}