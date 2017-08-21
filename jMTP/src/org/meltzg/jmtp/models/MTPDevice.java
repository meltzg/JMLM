package org.meltzg.jmtp.models;

/**
 * Represents an MTP device
 * 
 * @author Greg Meltzer
 *
 */
public class MTPDevice {
	private String deviceId;
	private String friendlyName;
	private String description;
	private String manufacturer;
	
	/**
	 * @param deviceId
	 * @param friendlyName
	 * @param description
	 * @param manufacturer
	 */
	public MTPDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		this.deviceId = deviceId;
		this.friendlyName = friendlyName;
		this.description = description;
		this.manufacturer = manufacturer;
	}

	/**
	 * @return
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * @param friendlyName
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@Override
	public String toString() {
		return "MTPDevice [deviceId=" + deviceId + ", friendlyName=" + friendlyName + ", description=" + description
				+ ", manufacturer=" + manufacturer + "]";
	}	
}
