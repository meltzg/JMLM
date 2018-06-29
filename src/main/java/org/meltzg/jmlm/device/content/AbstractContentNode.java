package org.meltzg.jmlm.device.content;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * An abstract representation of a content node.  This can be used to represent dir/file hierarchies.
 *
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public abstract class AbstractContentNode
        implements JsonSerializer<AbstractContentNode>, JsonDeserializer<AbstractContentNode> {
    public static final String ROOT_ID = "DEVICE";

    protected String id;
    /**
     * The ID of this node's parent
     */
    protected String pId;
    protected String origName;
    protected boolean isDir;
    /**
     * The size of the content in bytes
     */
    protected BigInteger size;
    /**
     * The storage capacity of this node in bytes
     */
    protected BigInteger capacity;
    protected Map<String, AbstractContentNode> children;

    protected boolean isValid;

    public AbstractContentNode() {
        this.children = new HashMap<>();
    }

    public AbstractContentNode(String id) {
        this.id = id;
        this.children = new HashMap<>();
    }

    public AbstractContentNode(String id, String pId, String origName, boolean isDir, BigInteger size, BigInteger capacity) {
        this.id = id;
        this.pId = pId;
        this.origName = origName;
        this.isDir = isDir;
        this.size = size;
        this.capacity = capacity;
        this.children = new HashMap<>();
    }

    /**
     * @return boolean for if this node is a directory
     */
    public boolean isDir() {
        return isDir;
    }

    /**
     * @return the node's ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return the node's parent's ID
     */
    public String getPId() {
        return pId;
    }

    /**
     * Sets this node's parent ID. Useful if the ID the device uses is inaccurate
     *
     * @param pId
     */
    public void setPId(String pId) {
        this.pId = pId;
    }

    /**
     * @return the name of this node
     */
    public String getOrigName() {
        return origName;
    }

    /**
     * @return the size of this node's content in bytes
     */
    public BigInteger getSize() {
        return size;
    }

    /**
     * @return the storage capacity of this node
     */
    public BigInteger getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity metadata for this node.
     *
     * @param capacity
     */
    public void setCapacity(BigInteger capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the children of this node
     */
    public Collection<AbstractContentNode> getChildren() {
        return children.values();
    }

    /**
     * Adds a child to this node's children
     *
     * @param node the node to add as a child
     * @return false if this node already has a child with the node's id
     */
    public boolean addChild(AbstractContentNode node) {
        if (node != null && !children.containsKey(node.getId())) {
            node.setPId(this.getId());
            children.put(node.getId(), node);
            return true;
        }
        return false;
    }

    /**
     * Removes a child from this node's children
     *
     * @param id the id of the child to remove
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
     * Retrieve a child node by ID
     * @param id
     * @return
     */
    public AbstractContentNode getChild(String id) {
        return children.get(id);
    }

    /**
     * Retrieves the first child whose name matches
     *
     * @param origName the name of the child to look for
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

    /**
     * @return the total size of this node and all of its decendants
     */
    public BigInteger getTotalSize() {
        BigInteger total = BigInteger.ZERO;

        Stack<AbstractContentNode> stack = new Stack<>();
        stack.add(this);
        while (!stack.empty()) {
            AbstractContentNode node = stack.pop();
            total = total.add(node.size);
            stack.addAll(node.getChildren());
        }

        return total;
    }

    @Override
    public JsonElement serialize(AbstractContentNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonMap = new JsonObject();
        Stack<AbstractContentNode> stack = new Stack<>();
        stack.push(src);
        while (!stack.empty()) {
            AbstractContentNode node = stack.pop();
            JsonElement serializedNode = node.serializeProperties();

            jsonMap.add(node.getId(), serializedNode);
            for (AbstractContentNode child : node.getChildren()) {
                stack.push(child);
            }
        }
        return jsonMap;
    }

    @Override
    public AbstractContentNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String rootId = null;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement pId = entry.getValue().getAsJsonObject().get("pId");
            if (rootId == null) {
                rootId = entry.getKey();
            } else if (pId == null) {
                throw new JsonParseException("JSON Object has multiple nodes without a pId");
            }
        }

        Stack<String> stack = new Stack<>();
        Map<String, AbstractContentNode> nodes = new HashMap<>();
        stack.push(rootId);

        while (!stack.empty()) {
            String id = stack.pop();
            JsonObject jsonNode = jsonObject.get(id).getAsJsonObject();
            AbstractContentNode node = getInstance();
            try {
                node.deserializeProperties(jsonNode);
                if (node.getPId() != null) {
                    AbstractContentNode parent = nodes.get(node.getPId());
                    if (parent == null) {
                        throw new JsonParseException(String.format("Node with id=%s does not exist", node.getPId()));
                    }
                    parent.addChild(node);
                }
                nodes.put(node.getId(), node);
                for (JsonElement child : jsonNode.getAsJsonArray("children")) {
                    stack.push(child.getAsString());
                }
            } catch (NullPointerException e) {
                throw new JsonParseException(e.getMessage());
            }
        }

        return nodes.get(rootId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!this.getClass().equals(other.getClass())) {
            return false;
        }

        AbstractContentNode oRoot = (AbstractContentNode) other;
        Stack<AbstractContentNode> thisStack = new Stack<>();
        Stack<AbstractContentNode> otherStack = new Stack<>();

        thisStack.push(this);
        otherStack.push(oRoot);

        while (!thisStack.empty() && !otherStack.empty()) {
            AbstractContentNode thisNode = thisStack.pop();
            AbstractContentNode otherNode = otherStack.pop();
            if (!thisNode.equalProps(otherNode)) {
                return false;
            }

            for (Map.Entry<String, AbstractContentNode> thisChild : thisNode.children.entrySet()) {
                thisStack.push(thisChild.getValue());
                otherStack.push(otherNode.children.get(thisChild.getKey()));
            }
        }

        if (!thisStack.empty() || !otherStack.empty()) {
            return false;
        }

        return true;
    }

    protected JsonElement serializeProperties() {
        JsonObject serialized = new JsonObject();
        serialized.addProperty("id", id);
        serialized.addProperty("pId", pId);
        serialized.addProperty("origName", origName);
        serialized.addProperty("isDir", isDir);
        serialized.addProperty("size", size);
        serialized.addProperty("capacity", capacity);
        serialized.addProperty("isValid", isValid);

        JsonArray childIds = new JsonArray();
        for (String id : children.keySet()) {
            childIds.add(id);
        }
        serialized.add("children", childIds);

        return serialized;
    }

    protected void deserializeProperties(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();

        id = jsonObject.get("id").getAsString();
        JsonElement jsonPId = jsonObject.get("pId");
        pId = jsonPId != null ? jsonPId.getAsString() : null;
        origName = jsonObject.get("origName").getAsString();
        isDir = jsonObject.get("isDir").getAsBoolean();
        size = new BigInteger(jsonObject.get("size").getAsString());
        capacity = new BigInteger(jsonObject.get("capacity").getAsString());
        isValid = jsonObject.get("isValid").getAsBoolean();
        children = new HashMap<>();
    }

    protected boolean equalProps(AbstractContentNode other) {
        if (other == null) {
            return false;
        }
        if (!id.equals(other.id) ||
                !origName.equals(other.origName) ||
                isDir != other.isDir ||
                !size.equals(other.size) ||
                !capacity.equals(other.capacity) ||
                isValid != other.isValid) {
            return false;
        }

        if (pId == null) {
            if (other.pId != null) {
                return false;
            }
        } else if (!pId.equals(other.pId)) {
            return false;
        }

        if (children.size() != other.children.size()) {
            return false;
        }

        if (!children.keySet().equals(other.children.keySet())) {
            return false;
        }

        return true;
    }

    protected abstract AbstractContentNode getInstance();
}