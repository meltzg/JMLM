package org.jmlm.device.content;

import java.util.ArrayList;
import java.util.List;

import org.omg.IOP.ProfileIdHelper;

public abstract class AbstractContentNode {
    protected String id;
    protected String pId;
    protected String origName;
    protected boolean isDir;
    protected List<AbstractContentNode> children;

    public AbstractContentNode() {}
    
    public AbstractContentNode(String id, String pId, String origName, boolean isDir) {
        this.id = id;
        this.pId = pId;
        this.origName = origName;
        this.isDir = isDir;
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

    public String getOrigName() {
        return origName;
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