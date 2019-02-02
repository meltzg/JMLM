package org.meltzg.jmlm.sync;

import lombok.Getter;
import org.meltzg.jmlm.device.content.AudioContent;

import java.util.UUID;

public class ContentSyncStatus {
    @Getter private final AudioContent contentInfo;
    @Getter private String contentId;
    @Getter private UUID mainLibrary;
    @Getter private UUID deviceLibrary;

    public ContentSyncStatus(AudioContent contentInfo, UUID mainLibrary, UUID deviceLibrary) {
        this.contentInfo = contentInfo;
        this.contentId = contentInfo.getId();
        this.mainLibrary = mainLibrary;
        this.deviceLibrary = deviceLibrary;
    }

    public boolean isInLibrary() {
        return mainLibrary != null;
    }

    public boolean isOnDevice() {
        return deviceLibrary != null;
    }

    public static ContentSyncStatus reverse(ContentSyncStatus status) {
        return new ContentSyncStatus(status.contentInfo, status.deviceLibrary, status.mainLibrary);
    }
}
