package org.meltzg.jmlm.device.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ContentRoot extends ContentTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1751834230504015884L;

	protected Map<String, ContentTree> idToNodes;

	public ContentRoot(ContentTree root) {
		super(root);
		buildRootInfo();
	}
	
	public Map<String, ContentTree> getIdToNodes() {
		return this.idToNodes;
	}
	
	private void buildRootInfo() {
		idToNodes = new HashMap<String, ContentTree>();
		Stack<ContentTree> nodes = new Stack<ContentTree>();
		
		nodes.push(this);
		while (!nodes.empty()) {
			ContentTree node = nodes.pop();
			idToNodes.put(node.getId(), node);
			
			if (node.getChildren() != null) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					nodes.push(node.getChildren().get(i));
				}
			}
		}
	}
}
