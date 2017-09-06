package org.meltzg.jmlm.device.models;

import java.util.List;

import org.meltzg.jmlm.content.models.AbstractContentTree;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.content.models.MTPContentTree;
import org.meltzg.jmlm.device.access.MTPContentInterface;

/**
 * Represents an MTP device
 * 
 * @author Greg Meltzer
 *
 */
public class MTPContentDevice extends AbstractContentDevice {
	
	MTPContentInterface j;

	public MTPContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		super(deviceId, friendlyName, description, manufacturer);
		
		j = MTPContentInterface.getInstance();
	}

	@Override
	public String toString() {
		return "MTPDevice [deviceId=" + deviceId + ", friendlyName=" + friendlyName + ", description=" + description
				+ ", manufacturer=" + manufacturer + "]";
	}

	@Override
	public void buildContentRoot() {
		if (j.selectDevice(this.deviceId)) {
			MTPContentTree oTree = j.getDeviceContent();
			if (oTree != null) {
				this.contentRoot = new ContentRoot(oTree);
			}
		}		
	}

	@Override
	public MTPContentTree transferToDevice(String filepath, String destId, String destName) {
		MTPContentTree newSubTree = null;
		if (j.selectDevice(deviceId)) {
			newSubTree = j.transferToDevice(filepath, destId, destName);
		}
		if (newSubTree != null) {
			if (this.contentRoot == null) {
				buildContentRoot();
			} else {
				this.contentRoot.getIdToNodes().get(newSubTree.getParentId()).getChildren().add(newSubTree);
				this.contentRoot.buildRootInfo();
			}
		}
		return newSubTree;
	}

	@Override
	public String removeFromDevice(String id, String stopId) {
		String highestRemoved = null;
		if (j.selectDevice(deviceId)) {
			highestRemoved = j.removeFromDevice(id, stopId);
		}
		if (highestRemoved != null) {
			if (this.contentRoot == null) {
				buildContentRoot();
			} else {
				String parentId = this.contentRoot.getIdToNodes().get(highestRemoved).getParentId();
				List<AbstractContentTree> children = this.contentRoot.getIdToNodes().get(parentId).getChildren();
				for (int i = 0; i < children.size(); i++) {
					if (children.get(i).getId().equals(highestRemoved)) {
						children.remove(i);
						break;
					}
				}
				this.contentRoot.buildRootInfo();
			}
		}
		return highestRemoved;
	}

	@Override
	public boolean transferFromDevice(String id, String destFilepath) {
		boolean success = false;
		if (j.selectDevice(deviceId)) {
			success = j.transferFromDevice(id, destFilepath);
		}
		return success;
	}

	@Override
	protected boolean removeFromDevice(String id) {
		String ret = removeFromDevice(id, null);
		return ret != null;
	}	
}
