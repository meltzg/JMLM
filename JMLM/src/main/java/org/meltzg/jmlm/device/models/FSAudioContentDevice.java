package org.meltzg.jmlm.device.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
			
			int lastSlash = destName.lastIndexOf('/');
			String destFolder = destName.substring(0, lastSlash);
			String destFile = destName.substring(lastSlash);
			FolderPair folderPair = createFolder(destId, destFolder);
			if (folderPair != null && folderPair.lastId != null) {
				FSAudioContentTree parent = ((FSAudioContentTree) contentRoot.getIdToNodes().get(folderPair.lastId));
				String transferPath = parent.getPath() + destFile;
				Files.copy(toTransfer.toPath(), Paths.get(transferPath));
				FSAudioContentTree newNode = FSAudioContentTree.createNode(parent.getId(), new File(transferPath));
				if (newNode != null) {
					parent.getChildren().add(newNode);
					contentRoot.buildRootInfo();
					if (folderPair.createdId != null) {
						newSubTree = (FSAudioContentTree) contentRoot.getIdToNodes().get(folderPair.createdId);
					} else {
						newSubTree = newNode;
					}
				} else {
					System.err.println("!!! Failed to transfer file to device: " + filepath);
				}
			} else {
				System.err.println("!!! Failed to create intermediate folders: " + destFolder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newSubTree;
	}

	@Override
	public boolean transferFromDevice(String id, String destFilepath) {
		boolean success = false;
		try {
			validateId(id);
			Path filepath = Paths.get(((FSAudioContentTree) (this.contentRoot.getIdToNodes().get(id))).getPath());
			Path destpath = Paths.get(destFilepath);
			
			int lastSlash = destFilepath.replace('\\', '/').lastIndexOf('/');
			(new File(destFilepath.substring(0, lastSlash))).mkdirs();

			Files.copy(filepath, destpath, StandardCopyOption.REPLACE_EXISTING);

			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	@Override
	protected boolean removeFromDevice(String id) {
		boolean success = false;
		try {
			validateId(id);
			AbstractContentTree deleteNode = this.contentRoot.getIdToNodes().get(id);
			if (deleteNode.getId().equals(contentRoot.getId())) {
				throw new IllegalArgumentException("Cannot delete device root");
			} else if (deleteNode.getChildren().size() > 0) {
				throw new IllegalArgumentException("Cannot delete object with children");
			} else {
				Path filepath = Paths.get(((FSAudioContentTree) deleteNode).getPath());
				Files.delete(filepath);
				List<AbstractContentTree> children = this.contentRoot.getIdToNodes().get(deleteNode.getParentId()).getChildren();
				for (int i = 0; i < children.size(); i++) {
					if (children.get(i).getId().equals(id)) {
						children.remove(i);
						break;
					}
				}
				contentRoot.buildRootInfo();
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	FolderPair createFolder(String destId, String path) {
		FolderPair folderPair = new FolderPair();
		try {
			validateId(destId);
			Queue<FSAudioContentTree> newFolders = new LinkedList<FSAudioContentTree>();
			FSAudioContentTree newRoot = null;
			String fullPath = ((FSAudioContentTree) contentRoot.getIdToNodes().get(destId)).getPath();
			String[] pathParts = path.split("/");
			String tmpId = destId;
			
			for (String part : pathParts) {
				String existingId = getObjIdByOrigName(tmpId, part);
				fullPath += "/" + part;
				if (existingId == null) {
					File f = new File(fullPath);
					if (!f.mkdir()) {
						System.err.println("!!! Failed to create folder: " + fullPath);
						folderPair = null;
						break;
					}
					
					FSAudioContentTree tmpNode = FSAudioContentTree.createNode(tmpId, f);
					tmpId = tmpNode.getId();
					if (newRoot == null) {
						newRoot = tmpNode;
					}
					newFolders.add(tmpNode);
					
					if (folderPair.createdId == null) {
						folderPair.createdId = tmpId;
					}
				} else {
					tmpId = existingId;
				}
			}
			
			if (folderPair != null) {
				folderPair.lastId = tmpId;
			}
			
			if (!newFolders.isEmpty()) {
				FSAudioContentTree tmpNode = newFolders.poll();
				while (tmpNode != null && !newFolders.isEmpty()) {
					FSAudioContentTree child = newFolders.poll();
					tmpNode.getChildren().add(child);
					tmpNode = child;
				}
				contentRoot.getIdToNodes().get(newRoot.getParentId()).getChildren().add(newRoot);
				contentRoot.buildRootInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return folderPair;
	}
	
	private class FolderPair {
		public String createdId;	// when creating folders, this is the highest folder created (null if none created)
		public String lastId;		// when creating folders this should be the ID of the last folder created/returned
	}
}
