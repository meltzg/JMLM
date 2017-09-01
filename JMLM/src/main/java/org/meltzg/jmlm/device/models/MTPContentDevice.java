package org.meltzg.jmlm.device.models;

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
}