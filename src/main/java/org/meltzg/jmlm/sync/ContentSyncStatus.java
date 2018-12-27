package org.meltzg.jmlm.sync;

public class ContentSyncStatus {
    private String contentId;
    private boolean inLibrary;
    private boolean onDevice;

    public ContentSyncStatus(String contentId, boolean inLibrary, boolean onDevice) {
        this.contentId = contentId;
        this.inLibrary = inLibrary;
        this.onDevice = onDevice;
    }

    public String getContentId() {
        return contentId;
    }

    public boolean isInLibrary() {
        return inLibrary;
    }

    public boolean isOnDevice() {
        return onDevice;
    }
}
