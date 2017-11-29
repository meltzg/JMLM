package org.jmlm.device.content;

import java.util.List;

public abstract class AbstractContentNode {
    protected String id;
    protected boolean isDir;
    protected List<AbstractContentNode> children;

    public boolean isDir() {
        return isDir;
    }

    public String getId() {
        return id;
    }

    public List<AbstractContentNode> getChildren() {
        return children;
    }
}