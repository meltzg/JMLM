package org.meltzg.jmlm.device;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;

/**
 * Represents an MTP device, such as the AK100ii
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class MTPContentDevice extends AbstractContentDevice {

	protected MTPDeviceInfo deviceInfo;

	public MTPContentDevice(String id) {
		this.deviceInfo = getDeviceInfo(id);
		this.deviceId = this.deviceInfo.deviceId;
		this.content = new ContentRootWrapper(readDeviceContent(AbstractContentNode.ROOT_ID));
	}

	/** Returns information on all MTP devices attached to the computer */
	public static native List<MTPDeviceInfo> getDevicesInfo();
	public static native MTPDeviceInfo getDeviceInfo(String id);

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
	
	@Override
	protected void assignLibCapacities() {
		// MTP Devices have objects representing storage devices (SD cards, internal storage)
		// The MTPContentNode represents these as directories with a non-zero capacity
		List<AbstractContentNode> storageDevices = new ArrayList<AbstractContentNode>();
		Map<String, List<AbstractContentNode>> storageDeviceMap = new HashMap<String, List<AbstractContentNode>>();

		Stack<AbstractContentNode> stack = new Stack<AbstractContentNode>();
		
		// Find the storage devices
		stack.add(content.getNode(AbstractContentNode.ROOT_ID));
		while (!stack.empty()) {
			AbstractContentNode node = stack.pop();
			if (node.getCapacity().compareTo(BigInteger.ZERO) > 0) {
				storageDevices.add(node);
			} else {
				stack.addAll(node.getChildren());
			}
		}
		
		for (String libRoot: libRoots) {
			for (AbstractContentNode device : storageDevices) {
				ContentRootWrapper wrapper = new ContentRootWrapper(device);
				if (wrapper.contains(libRoot)) {
					if (!storageDeviceMap.containsKey(device.getId())) {
						storageDeviceMap.put(device.getId(), new ArrayList<AbstractContentNode>());
					}
					storageDeviceMap.get(device.getId()).add(content.getNode(libRoot));
				}
			}
		}
		
		for (Map.Entry<String, List<AbstractContentNode>> storageMapping : storageDeviceMap.entrySet()) {
			BigInteger cap = content.getNode(storageMapping.getKey()).getCapacity();
			cap = cap.divide(BigInteger.valueOf(storageMapping.getValue().size()));
			for (AbstractContentNode node : storageMapping.getValue()) {
				node.setCapacity(cap);
			}
		}
	}

	private native List<String> getChildIds(String dId, String pId);

	private native AbstractContentNode createDirNode(String dId, String pId, String name);

	private native AbstractContentNode createContentNode(String dId, String pId, String file);

	private native AbstractContentNode readNode(String dId, String id);

	private native AbstractContentNode copyNode(String dId, String pId, String id, String tmpFolder);

	private native boolean deleteNode(String dId, String id);

	private native boolean retrieveNode(String dId, String id, String destFolder);

	public class MTPDeviceInfo {
		public final String deviceId;
		public final String friendlyName;
		public final String description;
		public final String manufacturer;

		public MTPDeviceInfo(String deviceId, String friendlyName, String description, String manufacturer) {
			this.deviceId = deviceId;
			this.friendlyName = friendlyName;
			this.description = description;
			this.manufacturer = manufacturer;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MTPDeviceInfo)) {
				return false;
			}

			MTPDeviceInfo oInfo = (MTPDeviceInfo) other;
			if (!this.deviceId.equals(oInfo.deviceId)) {
				return false;
			}
			if (!this.description.equals(oInfo.description)) {
				return false;
			}
			if (!this.manufacturer.equals(oInfo.manufacturer)) {
				return false;
			}
			if (!this.friendlyName.equals(oInfo.friendlyName)) {
				return false;
			}

			return true;
		}
	}

	static {
		System.loadLibrary("CLibJMTP");
	}
}