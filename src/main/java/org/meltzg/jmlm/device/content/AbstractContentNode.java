package org.meltzg.jmlm.device.content;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContentNode {
    protected String id;
    protected String pId;
    protected String origName;
    protected boolean isDir;
    protected long size;
    protected long capacity;
    protected Map<String, AbstractContentNode> children;

    public AbstractContentNode() {}
    
    public AbstractContentNode(String id) {
        this.id = id;
        this.children = new HashMap<String, AbstractContentNode>();
    }

    public boolean isDir() {
        return isDir;
    }

    public String getId() {
        return id;
    }

    public String getPId() {
        return pId;
    }

    public void setPId(String pId) {
        this.pId = pId;
	}

    public String getOrigName() {
        return origName;
    }

    public long getSize() {
        return size;
    }

    public long getCapacity() {
        return capacity;
    }

    public Collection<AbstractContentNode> getChildren() {
        return children.values();
    }

    public boolean addChild(AbstractContentNode node) {
        if (!children.containsKey(node.getId())) {
            node.setPId(this.getId());
            children.put(node.getId(), node);
            return true;
        }
        return false;
    }

    public boolean removeChild(String id) {
        if (children.containsKey(id)) {
            children.remove(id);
            return true;
        }
        return false;
    }

    public AbstractContentNode getChildByOName(String origName) {
        for (AbstractContentNode child : children.values()) {
            if (child.origName.equals(origName)) {
                return child;
            }
        }
        return null;
    }
}