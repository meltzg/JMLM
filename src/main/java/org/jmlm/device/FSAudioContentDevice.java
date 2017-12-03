package org.jmlm.device;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jmlm.device.content.AbstractContentNode;

public class FSAudioContentDevice extends AbstractContentDevice {

	@Override
	protected List<String> getChildIds(String pId) {
        List<String> childIds = new ArrayList<String>();
        File[] children = (new File(pId)).listFiles();
        for (File child : children) {
            childIds.add(child.getAbsolutePath());
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
		return null;
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