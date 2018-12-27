package org.meltzg.jmlm.sync;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DeviceSyncManager {
    private FileSystemAudioContentDevice mainLibrary;
    private FileSystemAudioContentDevice attachedDevice;
    private Map<String, ContentSyncStatus> syncStatuses;


    public DeviceSyncManager(FileSystemAudioContentDevice mainLibrary, FileSystemAudioContentDevice attachedDevice) {
        this.mainLibrary = mainLibrary;
        this.attachedDevice = attachedDevice;
        this.syncStatuses = null;

        refreshSyncStatus();
    }

    public void refreshSyncStatus() {
        syncStatuses = new HashMap<>();
        var allContent = new HashSet<String>();
        allContent.addAll(mainLibrary.getContent().keySet());
        allContent.addAll(attachedDevice.getContent().keySet());

        for (var id : allContent) {
            var syncStatus = new ContentSyncStatus(id, mainLibrary.containsContent(id),
                    attachedDevice.containsContent(id));
            syncStatuses.put(id, syncStatus);
        }
    }


    public Map<String, ContentSyncStatus> getSyncStatuses() {
        return syncStatuses;
    }

    public ContentSyncStatus getSyncStatus(String id) {
        var status = this.syncStatuses.get(id);
        if (status == null) {
            status = new ContentSyncStatus(id, false, false);
        }
        return status;
    }
}
