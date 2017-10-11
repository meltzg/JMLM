/**
 * 
 */
package org.meltzg.jmlm.device.models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.content.models.MTPContentTree;
import org.meltzg.jmlm.device.access.IContentInterface;

/**
 * @author vader
 *
 */
public abstract class AbstractContentDevice implements IContentInterface {

	protected String deviceId;
	protected String friendlyName;
	protected String description;
	protected String manufacturer;
	protected Map<String, ContentRoot> libraryRoots;
	protected ContentRoot contentRoot;

	/**
	 * @param deviceId
	 * @param friendlyName
	 * @param description
	 * @param manufacturer
	 */
	public AbstractContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		this.deviceId = deviceId;
		this.friendlyName = friendlyName;
		this.description = description;
		this.manufacturer = manufacturer;

		libraryRoots = new HashMap<String, ContentRoot>();
	}

	public abstract void buildContentRoot();

	public void addContentRoot(String rootId) {
		try {
			validateId(rootId);
			if (libraryRoots.get(rootId) != null) {
				throw new IllegalArgumentException("Device already has a library root with ID: " + rootId);
			}

			libraryRoots.put(rootId, new ContentRoot(contentRoot.getIdToNodes().get(rootId)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void validateId(String id) {
		if (contentRoot == null) {
			throw new NullPointerException("Device content has not been initialized.");
		}
		if (id == null) {
			throw new NullPointerException("ID cannot be null");
		}
		if (contentRoot.getIdToNodes().get(id) == null) {
			throw new NullPointerException("Device does not contain an object with ID: " + id);
		}
	}

	protected String getObjIdByOrigName(String parentId, String origName) {
		String objId = null;
		try {
			validateId(parentId);
			List<AbstractContentTree> pChildren = contentRoot.getIdToNodes().get(parentId).getChildren();
			for (AbstractContentTree child : pChildren) {
				if (child.getOrigName().equals(origName)) {
					objId = child.getId();
					break;
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return objId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Map<String, ContentRoot> getLibraryRoots() {
		return libraryRoots;
	}

	public ContentRoot getContentRoot() {
		return contentRoot;
	}

	@Override
	public AbstractContentTree getDeviceContent() {
		return getDeviceContent(null);
	}

	@Override
	public AbstractContentTree getDeviceContent(String rootId) {
		if (this.contentRoot == null) {
			buildContentRoot();
		}

		if (this.contentRoot != null) {
			if (rootId == null) {
				rootId = this.contentRoot.getId();
			}

			return this.contentRoot.getIdToNodes().get(rootId);
		}
		return null;
	}

	@Override
	public String removeFromDevice(String id, String stopId) {
		String highestDeleted = null;

		try {
			validateId(id);

			Queue<String> parentIdsToDelete = new LinkedList<String>();
			Stack<String> idsToDelete;

			if (stopId != null) {
				boolean stopFound = false;
				AbstractContentTree node = this.contentRoot.getIdToNodes().get(id);
				while (!stopFound && !node.getParentId().isEmpty()) {
					if (node.getParentId().equals(stopId)) {
						stopFound = true;
					} else {
						parentIdsToDelete.add(node.getParentId());
					}

					node = this.contentRoot.getIdToNodes().get(node.getParentId());
				}

				if (!stopFound) {
					System.err.println("!!! Failed to find stopId.  No parent objects will be deleted");
					parentIdsToDelete.clear();
				}
			}

			idsToDelete = getContentIDStack(id);

			boolean ret = true;
			while (ret && !idsToDelete.isEmpty()) {
				String del = idsToDelete.pop();
				ret = removeFromDevice(del);
				if (ret) {
					highestDeleted = del;
				}
			}
			while (ret && !parentIdsToDelete.isEmpty()) {
				String del = parentIdsToDelete.poll();
				if (contentRoot.getIdToNodes().get(del).getChildren().isEmpty()) {
					ret = removeFromDevice(del);
					if (ret) {
						highestDeleted = del;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return highestDeleted;
	}

	protected abstract boolean removeFromDevice(String id);

	protected Stack<String> getContentIDStack(String rootId) {
		Stack<String> idStack = new Stack<String>();
		Stack<String> tmpStack = new Stack<String>();

		try {
			validateId(rootId);
			idStack.push(rootId);
			tmpStack.push(rootId);

			while (!tmpStack.isEmpty()) {
				String id = tmpStack.pop();
				for (AbstractContentTree node : contentRoot.getIdToNodes().get(id).getChildren()) {
					idStack.add(node.getId());
					tmpStack.add(node.getId());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return idStack;
	}
	
	@Override
	public AbstractContentTree moveOnDevice(String id, String destId, String destFolderPath, String tmpFolder) {
		AbstractContentTree moveTree = moveObject(id, destId, destFolderPath, tmpFolder);
		
		if (moveTree != null) {
			String oldParentId = this.contentRoot.getIdToNodes().get(id).getParentId();
			String newParentId = moveTree.getParentId();
			
			List<AbstractContentTree> children = this.contentRoot.getIdToNodes().get(oldParentId).getChildren();
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).getId().equals(id)) {
					children.remove(i);
					break;
				}
			}
			
			this.contentRoot.getIdToNodes().get(newParentId).getChildren().add(moveTree);
			
			this.contentRoot.buildRootInfo();		}
		
		return moveTree;
	}
	
	protected abstract AbstractContentTree moveObject(String id, String destId, String destFolderPath, String tmpFolder);
}
