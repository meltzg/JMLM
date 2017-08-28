package org.meltzg.jmtp.models;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MTPObjectRoot extends MTPObjectTree {
	
	private Map<String, MTPObjectTree> idToNodes;

	public MTPObjectRoot(String id, String parentId, String persistId, String name, String origName, BigInteger size,
			BigInteger capacity, List<MTPObjectTree> children) {
		super(id, parentId, persistId, name, origName, size, capacity, children);

		buildRootInfo();
	}

	public MTPObjectRoot(MTPObjectTree root) {
		super(root);
	}
	
	private void buildRootInfo() {
		idToNodes = new HashMap<String, MTPObjectTree>();
		Stack<MTPObjectTree> nodes = new Stack<MTPObjectTree>();
		
		nodes.push(this);
		while (!nodes.empty()) {
			MTPObjectTree node = nodes.pop();
			idToNodes.put(node.getId(), node);
			
			if (node.getChildren() != null) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					nodes.push(node.getChildren().get(i));
				}
			}
		}
	}
}
