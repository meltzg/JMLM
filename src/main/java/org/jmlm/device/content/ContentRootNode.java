package org.jmlm.device.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ContentRootNode extends AbstractContentNode {

    AbstractContentNode root;
    Map<String, AbstractContentNode> idToNodes;
    
    public ContentRootNode(AbstractContentNode root) {
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