package org.meltzg.jmlm.device.content;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract representation of a content node.  This can be used to represent dir/file hierarchies.
 * 
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public abstract class AbstractContentNode {
	protected static final String ROOT_ID = "DEVICE";

    protected String id;
    /** The ID of this node's parent */
    protected String pId;
    protected String origName;
    protected boolean isDir;
    /** The size of the content in bytes */
    protected long size;
    /** The storage capacity of this node in bytes */
    protected long capacity;
    protected Map<String, AbstractContentNode> children;

    public AbstractContentNode() {}
    
    public AbstractContentNode(String id) {
        this.id = id;
        this.children = new HashMap<String, AbstractContentNode>();
    }

    /** @return boolean for if this node is a directory */
    public boolean isDir() {
        return isDir;
    }

    /** @return the node's ID */
    public String getId() {
        return id;
    }
    
    /** @return the node's parent's ID */
    public String getPId() {
        return pId;
    }

    /** 
     * Sets this node's parent ID. Useful if the ID the device uses is inaccurate
     * @param pId
     */
    public void setPId(String pId) {
        this.pId = pId;
	}

    /** @return the name of this node */
    public String getOrigName() {
        return origName;
    }

    /** @return the size of this node's content in bytes */
    public long getSize() {
        return size;
    }

    /** @return the storage capacity of this node */
    public long getCapacity() {
        return capacity;
    }

    /** @return the children of this node */
    public Collection<AbstractContentNode> getChildren() {
        return children.values();
    }

    /**
     * Adds a child to this node's children
     * @param node the node to add as a child
     * @return true if the child was successfully added
     * @return false if this node already has a child with the node's id
     */
    public boolean addChild(AbstractContentNode node) {
        if (!children.containsKey(node.getId())) {
            node.setPId(this.getId());
            children.put(node.getId(), node);
            return true;
        }
        return false;
    }

    /**
     * Removes a child from this node's children
     * @param id the id of the child to remove
     * @return true if the child was successfully removed
     * @return false if this node has no child with the given ID
     */
    public boolean removeChild(String id) {
        if (children.containsKey(id)) {
            children.remove(id);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the first child whose name matches
     * @param origName the name of the child to look for
     * @return The first node with a matching origName
     * @return null if no child is found with the given name
     */
    public AbstractContentNode getChildByOName(String origName) {
        for (AbstractContentNode child : children.values()) {
            if (child.origName.equals(origName)) {
                return child;
            }
        }
        return null;
    }
}