package org.meltzg.jmlm.device.content;

import com.google.gson.Gson;

import java.math.BigInteger;
import java.util.HashMap;

public class TestContentNode extends AbstractContentNode {
    public TestContentNode() {
        super();
    }
    public TestContentNode(String id, String pId, String origName, boolean isDir, BigInteger size, BigInteger capacity) {
        super( id, pId, origName, isDir, size, capacity);
    }

    public TestContentNode(AbstractContentNode other) {
        this(other.id, other.pId, other.origName, other.isDir, other.size, other.capacity);
    }
}
