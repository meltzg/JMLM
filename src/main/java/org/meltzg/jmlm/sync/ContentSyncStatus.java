package org.meltzg.jmlm.sync;

import java.util.UUID;

public class ContentSyncStatus {
    private String contentId;
    private UUID mainLibrary;
    private UUID deviceLibrary;

    public ContentSyncStatus(String contentId, UUID mainLibrary, UUID deviceLibrary) {
        this.contentId = contentId;
        this.mainLibrary = mainLibrary;
        this.deviceLibrary = deviceLibrary;
    }

    public String getContentId() {
        return contentId;
    }

    public UUID getMainLibrary() { return mainLibrary; }

    public UUID getDeviceLibrary() { return deviceLibrary; }

    public boolean isInLibrary() {
        return mainLibrary != null;
    }

    public boolean isOnDevice() {
        return deviceLibrary != null;
    }
}
