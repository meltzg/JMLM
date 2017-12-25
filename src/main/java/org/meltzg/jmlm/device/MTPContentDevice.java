package org.meltzg.jmlm.device;

import java.io.File;
import java.util.List;
import org.meltzg.jmlm.device.content.AbstractContentNode;

/**
 * Represents an MTP device, such as the AK100ii
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class MTPContentDevice extends AbstractContentDevice {

    protected MTPDeviceInfo deviceInfo;

	/** Returns information on all MTP devices attached to the computer */
	public static native List<MTPDeviceInfo> getDevicesInfo();

	@Override
	protected List<String> getChildIds(String pId) {
		return getChildIds(this.deviceId, pId);
	}

	@Override
	protected AbstractContentNode createDirNode(String pId, String name) {
		return createDirNode(this.deviceId, pId, name);
	}

	@Override
	protected AbstractContentNode createContentNode(String pId, File file) {
		return createContentNode(this.deviceId, pId, file.getAbsolutePath());
	}

	@Override
	protected AbstractContentNode readNode(String id) {
		return readNode(this.deviceId, id);
	}

	@Override
	protected AbstractContentNode copyNode(String pId, String id, String tmpFolder) {
		return copyNode(this.deviceId, pId, id, tmpFolder);
	}

	@Override
	protected boolean deleteNode(String id) {
		return deleteNode(this.deviceId, id);
	}

	@Override
	protected boolean retrieveNode(String id, String destFolder) {
		return retrieveNode(this.deviceId, id, destFolder);
	}

	private native List<String> getChildIds(String dId, String pId);

	private native AbstractContentNode createDirNode(String dId, String pId, String name);

	private native AbstractContentNode createContentNode(String dId, String pId, String file);

	private native AbstractContentNode readNode(String dId, String id);

	private native AbstractContentNode copyNode(String dId, String pId, String id, String tmpFolder);

	private native boolean deleteNode(String dId, String id);

    private native boolean retrieveNode(String dId, String id, String destFolder);
    
    public class MTPDeviceInfo {
		public String deviceId;
        public String friendlyName;
        public String description;
        public String manufacturer;
    }

    static {
		System.loadLibrary("libJMTP");
	}
}