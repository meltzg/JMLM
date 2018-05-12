package org.meltzg.jmlm.device.content;

import java.math.BigInteger;

public class MTPContentNode extends AbstractContentNode {
    private String name;

    public MTPContentNode(String id, String pId, String name, String origName, boolean isDir, BigInteger size, BigInteger capacity) {
        super(id, pId, origName, isDir, size, capacity);
        this.name = name;
        this.isValid = true;
    }

    /**
     * Returns the underlying name of the content, which may be differnt than the
     * name of the content as seen in the browser
     *
     * @return name
     */
    public String getName() {
        return name;
    }
}
