package org.meltzg.jmlm.device;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;
import org.meltzg.jmlm.device.content.FSAudioContentNode;

public class FSAudioContentDevice extends AbstractContentDevice {

	public FSAudioContentDevice() {
		super();
		this.content = new ContentRootWrapper(new FSAudioContentNode());
	}

	@Override
	public boolean addLibraryRoot(String id) {
		String addId = id;
		if (!content.contains(id)) {
			AbstractContentNode node = readDeviceContent(id);
			content.addToRoot(node);
			content.refreshRootInfo();
			
			addId = node.getId();
		}

		return super.addLibraryRoot(addId);
	}
	
	@Override
	protected List<String> getChildIds(String pId) {
        List<String> childIds = new ArrayList<String>();
        File[] children = (new File(pId)).listFiles();
		if (children != null) {
			for (File child : children) {
				childIds.add(child.getAbsolutePath());
			}
		}
		
		return childIds;
	}

	@Override
	protected AbstractContentNode createDirNode(String pId, String name) {
		return null;
	}

	@Override
	protected AbstractContentNode createContentNode(String pId, File file) {
		return null;
	}

	@Override
	protected AbstractContentNode moveNode(String pId, String id, String tmpFolder) {
		return null;
	}

	@Override
	protected AbstractContentNode readNode(String id) {
		FSAudioContentNode node = null;
		File toRead = new File(id);
		if (!toRead.exists()) {
			System.err.println("Node with ID " + id + " could not be read. File does not exist");
		} else {
			node = new FSAudioContentNode(id);
			if (!node.isValid()) {
				node = null;
			}
		}
		return node;
	}

	@Override
	protected boolean deleteNode(String id) {
		return false;
	}

	@Override
	protected boolean retrieveNode(String id, String destFolder) {
		return false;
	}
}