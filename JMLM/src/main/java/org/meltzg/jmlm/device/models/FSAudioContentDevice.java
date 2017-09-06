package org.meltzg.jmlm.device.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Stack;

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
			FSAudioContentTree tmpRoot = new FSAudioContentTree(this.contentRoot.getId(), rootPath, rootPath,
					BigInteger.ZERO);

			// walk directory and retrieve nodes
			// Map<String, FSAudioContentTree> idToNodes = new HashMap<String,
			// FSAudioContentTree>();
			Stack<FSAudioContentTree> nodes = new Stack<FSAudioContentTree>();
			nodes.push(tmpRoot);
			while (!nodes.empty()) {
				FSAudioContentTree node = nodes.pop();

				// idToNodes.put(node.getId(), node);

				File[] children = new File(node.getPath()).listFiles();
				if (children == null) {
					continue;
				}

				for (File c : children) {
					FSAudioContentTree cNode = FSAudioContentTree.createNode(node.getId(), c);

					if (cNode != null) {
						nodes.push(cNode);
						node.getChildren().add(cNode);
					}
				}

			}

			this.contentRoot.getChildren().add(tmpRoot);
			this.contentRoot.buildRootInfo();
			this.libraryRoots.put(tmpRoot.getId(), new ContentRoot(tmpRoot));
		}
	}

	@Override
	public FSAudioContentTree transferToDevice(String filepath, String destId, String destName) {
		FSAudioContentTree newSubTree = null;
		try {
			validateId(destId);
			File toTransfer = new File(filepath);
			if (!toTransfer.exists()) {
				throw new FileNotFoundException(filepath);
			}
			if (toTransfer.isDirectory()) {
				throw new IllegalArgumentException("!!! Cannot transfer directory to device: " + filepath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newSubTree;
	}

	@Override
	public boolean transferFromDevice(String id, String destFilepath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean removeFromDevice(String id) {
		// TODO Auto-generated method stub
		return false;
	}
}
