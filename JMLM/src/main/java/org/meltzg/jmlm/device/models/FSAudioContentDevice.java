package org.meltzg.jmlm.device.models;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.content.models.FSAudioContentTree;

public class FSAudioContentDevice extends AbstractContentDevice {

	public FSAudioContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		super(deviceId, friendlyName, description, manufacturer);
		
	}

	@Override
	public void buildContentRoot() {
		this.contentRoot = new ContentRoot(new FSAudioContentTree("", "ROOT", "", BigInteger.ZERO));
	}
	
	@Override
	public void addContentRoot(String rootPath) {
		if (this.contentRoot == null) {
			buildContentRoot();
		}
		
		File tmp = new File(rootPath);
		if (!tmp.exists()) {
			System.err.println("!!! '" + rootPath + "' does not exist");
		} else if (!tmp.isDirectory()) {
			System.err.println("!!! '" + rootPath + "' is not a directory");
		} else {
			FSAudioContentTree tmpRoot = new FSAudioContentTree(this.contentRoot.getId(), rootPath, rootPath, BigInteger.ZERO);
			
			// walk directory and retrieve nodes
//			Map<String, FSAudioContentTree> idToNodes = new HashMap<String, FSAudioContentTree>();
			Stack<FSAudioContentTree> nodes = new Stack<FSAudioContentTree>();
			nodes.push(tmpRoot);
			while (!nodes.empty()) {
				FSAudioContentTree node = nodes.pop();
				
//				idToNodes.put(node.getId(), node);
				
				File[] children = new File(node.getPath()).listFiles();
				if (children == null) {
					continue;
				}
				
				for (File c : children) {
					FSAudioContentTree cNode = new FSAudioContentTree(node.getId(),
							c.getName(),
							c.getAbsolutePath(),
							BigInteger.ZERO);

					if (!c.isDirectory()) {
						// TODO check if file is audio and get audio file metadata
						cNode.setSize(BigInteger.valueOf(c.length()));
					}
					
					nodes.push(cNode);
					node.getChildren().add(cNode);
				}
				
			}
			
			this.contentRoot.getChildren().add(tmpRoot);
			this.contentRoot.buildRootInfo();
			this.libraryRoots.put(tmpRoot.getId(), new ContentRoot(tmpRoot));
		}
	}
}
