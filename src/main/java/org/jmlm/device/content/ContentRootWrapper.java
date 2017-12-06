package org.jmlm.device.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ContentRootWrapper {

    AbstractContentNode root;
    Map<String, AbstractContentNode> idToNodes;
    
    public ContentRootWrapper(AbstractContentNode root) {
        this.root = root;
        refreshRootInfo();
    }
    
    public boolean contains(String id) {
		return idToNodes.containsKey(id);
	}

	public AbstractContentNode getNode(String id) {
        if (contains(id)) {
            return idToNodes.get(id);
        }
        return null;
    }

    public boolean addToRoot(AbstractContentNode node) {
        for (AbstractContentNode child : this.root.getChildren()) {
            if (child.getId().equals(node.getId())) {
                System.err.println("Content toot already has child with ID " + node.getId());
                return false;
            }
        }

        node.setPId(this.root.getId());
        this.root.getChildren().add(node);
        refreshRootInfo();
        return true;
    }
    
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
}