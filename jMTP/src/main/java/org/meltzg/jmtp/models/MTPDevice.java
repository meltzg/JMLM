package org.meltzg.jmtp.models;

import org.meltzg.jmlm.device.models.ContentDevice;
import org.meltzg.jmlm.device.models.ContentRoot;
import org.meltzg.jmtp.JMTP;

/**
 * Represents an MTP device
 * 
 * @author Greg Meltzer
 *
 */
public class MTPDevice extends ContentDevice {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3647677976999328571L;
	JMTP j;

	public MTPDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		super(deviceId, friendlyName, description, manufacturer);
		
		j = JMTP.getInstance();
	}

	@Override
	public String toString() {
		return "MTPDevice [deviceId=" + deviceId + ", friendlyName=" + friendlyName + ", description=" + description
				+ ", manufacturer=" + manufacturer + "]";
	}

	@Override
	public void buildContentRoot() {
		if (j.selectDevice(this.deviceId)) {
			MTPObjectTree oTree = j.getDeviceContent();
			if (oTree != null) {
				this.contentRoot = new ContentRoot(oTree);
			}
		}		
	}	
}
