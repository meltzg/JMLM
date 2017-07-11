package org.meltzg.jmtp.mtp;

public class MTPDevice {
	private String deviceId;
	private String friendlyName;
	private String description;
	private String manufacturer;
	
	public MTPDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		this.deviceId = deviceId;
		this.friendlyName = friendlyName;
		this.description = description;
		this.manufacturer = manufacturer;
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
	
	@Override
	public String toString() {
		return "{"
				+ "\tid: " + deviceId + ",\n"
				+ "\tfname: " + friendlyName + ",\n"
				+ "\tdesc: " + description + ",\n"
				+ "\tmanufacturer: " + manufacturer + ",\n"
				+ "}";
	}
}
