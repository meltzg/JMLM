/**
 * 
 */
package org.meltzg.jmlm.device.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vader
 *
 */
public abstract class ContentDevice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8721771175784020302L;

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
	public ContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		this.deviceId = deviceId;
		this.friendlyName = friendlyName;
		this.description = description;
		this.manufacturer = manufacturer;
	}
	
	public abstract void buildContentRoot();
	
	public void addContentRoot(String rootId) {
		if (contentRoot == null) {
			throw new NullPointerException("Device content has not been initialized.");
		}
		if (contentRoot.getIdToNodes().get(rootId) == null) {
			throw new NullPointerException("Device does not contain an object with ID: " + rootId);
		}
		if (rootId == null) {
			throw new NullPointerException("Root ID cannot be null");
		}
		if (libraryRoots == null) {
			libraryRoots = new HashMap<String, ContentRoot>();
		}
		if (libraryRoots.get(rootId) != null) {
			throw new IllegalArgumentException("Device already has a library root with ID: " + rootId);
		}
		
		libraryRoots.put(rootId, new ContentRoot(contentRoot.getIdToNodes().get(rootId)));
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
}
