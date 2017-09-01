package org.meltzg.jmlm.content.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ContentRoot extends AbstractContentTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1751834230504015884L;

	protected Map<String, AbstractContentTree> idToNodes;

	public ContentRoot(AbstractContentTree root) {
		super(root);
		buildRootInfo();
	}
	
	public Map<String, AbstractContentTree> getIdToNodes() {
		return this.idToNodes;
	}
	
	public void buildRootInfo() {
		idToNodes = new HashMap<String, AbstractContentTree>();
		Stack<AbstractContentTree> nodes = new Stack<AbstractContentTree>();
		
		nodes.push(this);
		while (!nodes.empty()) {
			AbstractContentTree node = nodes.pop();
			idToNodes.put(node.getId(), node);
			
			if (node.getChildren() != null) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					nodes.push(node.getChildren().get(i));
				}
			}
		}
	}
}
