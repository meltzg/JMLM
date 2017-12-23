package org.meltzg.jmlm.device.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.meltzg.jmlm.device.InvalidContentIDException;

/**
 * A wrapper for AbstractContentNodes that contains useful functions for managing the tree
 * 
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class ContentRootWrapper {

    AbstractContentNode root;
    Map<String, AbstractContentNode> idToNodes;
    
    public ContentRootWrapper(AbstractContentNode root) {
        this.root = root;
        refreshRootInfo();
    }
    
    /** 
     * Returns true if the content tree contains a node with the given ID
     * @param id ID of the node to search for
     * @return result of the check
     */
    public boolean contains(String id) {
		return idToNodes.containsKey(id);
	}

    /**
     * Retrieves a node by ID
     * @param id ID of the node to search for
     * @return the node if found, or null
     */
	public AbstractContentNode getNode(String id) {
        return idToNodes.get(id);
    }

    /**
     * Adds a node as a child of the content root.  Useful for devices that are abstractions
     * of existing devices (such as the FSAContentDevice)
     * 
     * @param node the node to add
     * @return true if successfully added, false if the content tree already has a node with the given ID 
     */
    public boolean addToRoot(AbstractContentNode node) {
        if (contains(node.getId())) {
            System.err.println("Content root already contains a node with ID " + node.getId());
            return false;
        }

        this.root.addChild(node);
        refreshRootInfo();
        return true;
    }
    
    /**
     * Rebuilds the ContentRootWrapper's content metadata.  This should be called after any change to a device's content
     */
    public void refreshRootInfo() {
        this.idToNodes = new HashMap<>();
        Stack<AbstractContentNode> stack = new Stack<AbstractContentNode>();
        stack.add(root);
        while (!stack.empty()) {
            AbstractContentNode node = stack.pop();
            idToNodes.put(node.getId(), node);

            for (AbstractContentNode child : node.getChildren()) {
                stack.add(child);
            }
        }
    }
    
    /**
     * Can be used to add a single node to the content root's metadata.
     * Useful for when an operation creates many nodes and refreshing everything becomes expensive.
     * @param node the node to add to the content metadata
     * @throws InvalidContentIDException if the node already exists in this's metadata
     */
    public void refreshRootInfo(AbstractContentNode node) throws InvalidContentIDException {
    	if (contains(node.getId())) {
    		throw new InvalidContentIDException("Content already has a node with ID " + node.getId());
        }
        if (idToNodes == null) {
            idToNodes = new HashMap<>();
        }
        idToNodes.put(node.getId(), node);
    }
}