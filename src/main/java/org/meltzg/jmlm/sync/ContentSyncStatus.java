package org.meltzg.jmlm.sync;

import org.meltzg.jmlm.device.content.AudioContent;

import java.util.UUID;

public class ContentSyncStatus {
    private final AudioContent contentInfo;
    private String contentId;
    private UUID mainLibrary;
    private UUID deviceLibrary;

    public ContentSyncStatus(AudioContent contentInfo, UUID mainLibrary, UUID deviceLibrary) {
        this.contentInfo = contentInfo;
        this.contentId = contentInfo.getId();
        this.mainLibrary = mainLibrary;
        this.deviceLibrary = deviceLibrary;
    }

    public AudioContent getContentInfo() {
        return contentInfo;
    }

    public String getContentId() {
        return contentId;
    }

    public UUID getMainLibrary() {
        return mainLibrary;
    }

    public UUID getDeviceLibrary() {
        return deviceLibrary;
    }

    public boolean isInLibrary() {
        return mainLibrary != null;
    }

    public boolean isOnDevice() {
        return deviceLibrary != null;
    }
}
