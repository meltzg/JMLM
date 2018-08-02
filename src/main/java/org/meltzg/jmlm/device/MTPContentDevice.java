package org.meltzg.jmlm.device;

import org.meltzg.jmlm.device.content.AbstractContentNode;
import org.meltzg.jmlm.device.content.ContentRootWrapper;
import org.meltzg.jmlm.device.storage.StorageDevice;

import java.io.File;
import java.util.List;

/**
 * Represents an MTP device, such as the AK100ii
 *
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class MTPContentDevice extends AbstractContentDevice {

    static {
        System.loadLibrary("jmtp");
        MTPContentDevice.initMTP();
    }

    protected MTPDeviceInfo deviceInfo;

    public MTPContentDevice(String id) {
        this.deviceInfo = getDeviceInfo(id);
        this.deviceId = this.deviceInfo.deviceId;
        this.content = new ContentRootWrapper(readDeviceContent(AbstractContentNode.ROOT_ID));
    }

    /**
     * Returns information on all MTP devices attached to the computer
     */
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
    protected StorageDevice getStorageDevice(String id) { return getStorageDevice(this.deviceId, id); }

    private static native void initMTP();

    private native List<String> getChildIds(String dId, String pId);

    private native AbstractContentNode createDirNode(String dId, String pId, String name);

    private native AbstractContentNode createContentNode(String dId, String pId, String file);

    private native AbstractContentNode readNode(String dId, String id);

    private native AbstractContentNode copyNode(String dId, String pId, String id, String tmpFolder);

    private native boolean deleteNode(String dId, String id);

    private native boolean retrieveNode(String dId, String id, String destFolder);

    private native  StorageDevice getStorageDevice(String dId, String id);

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
            return this.friendlyName.equals(oInfo.friendlyName);
        }
    }
}