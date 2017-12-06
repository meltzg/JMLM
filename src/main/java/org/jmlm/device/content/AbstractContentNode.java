package org.jmlm.device.content;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContentNode {
    protected String id;
    protected String pId;
    protected String origName;
    protected boolean isDir;
    protected long size;
    protected long capacity;
    protected List<AbstractContentNode> children;

    public AbstractContentNode() {}
    
    public AbstractContentNode(String id) {
        this.id = id;
        this.children = new ArrayList<AbstractContentNode>();
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

    public List<AbstractContentNode> getChildren() {
        return children;
    }

    public AbstractContentNode getChildByOName(String origName) {
        for (AbstractContentNode child : children) {
            if (child.origName.equals(origName)) {
                return child;
            }
        }
        return null;
    }
}