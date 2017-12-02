package org.jmlm.device.content;

import java.util.List;

public abstract class AbstractContentNode {
    protected String id;
    protected String origName;
    protected boolean isDir;
    protected List<AbstractContentNode> children;

    public boolean isDir() {
        return isDir;
    }

    public String getId() {
        return id;
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