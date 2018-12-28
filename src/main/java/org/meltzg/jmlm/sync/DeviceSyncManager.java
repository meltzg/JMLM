package org.meltzg.jmlm.sync;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;

import java.io.FileNotFoundException;
import java.util.*;

public class DeviceSyncManager {
    private FileSystemAudioContentDevice mainLibrary;
    private FileSystemAudioContentDevice attachedDevice;
    private Map<String, ContentSyncStatus> syncStatuses;
    private Set<String> allContent;


    public DeviceSyncManager(FileSystemAudioContentDevice mainLibrary, FileSystemAudioContentDevice attachedDevice) {
        this.mainLibrary = mainLibrary;
        this.attachedDevice = attachedDevice;
        this.syncStatuses = null;
        this.allContent = null;

        refreshSyncStatus();
    }

    public void refreshSyncStatus() {
        syncStatuses = new HashMap<>();
        allContent = new HashSet<>();

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

    public SyncPlan createSyncPlan(Set<String> desiredDeviceContent) throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException {
        return createSyncPlan(desiredDeviceContent, NotInLibraryStrategy.CANCEL_SYNC);
    }

    public SyncPlan createSyncPlan(Set<String> desiredDeviceContent, NotInLibraryStrategy strategy) throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException {
        var plan = new SyncPlan();
        var libCapacities = new ArrayList<>(attachedDevice.getLibraryRootCapacities().entrySet());

        if (libCapacities.isEmpty()) {
            throw new IllegalStateException("Attached device has no libraries configured");
        }

        var contentInfo = new LinkedList<AudioContent>();
        for (var contentId : desiredDeviceContent) {
            if (!allContent.contains(contentId)) {
                throw new FileNotFoundException("Content with ID " + contentId +
                        " is not in the library or the attached device");
            }
        }

        libCapacities.sort((lib1, lib2) -> Long.signum(lib2.getValue() - lib1.getValue()));
        contentInfo.sort((info1, info2) -> Long.signum(info2.getSize() - info1.getSize()));

        var contentDestinations = new HashMap<String, UUID>();
        for (var lib : libCapacities) {
            var libId = lib.getKey();
            var remainingCapacity = lib.getValue();
            var contentItr = contentInfo.iterator();
            while (contentItr.hasNext()) {
                var info = contentItr.next();
                if (info.getSize() <= remainingCapacity) {
                    contentDestinations.put(info.getId(), libId);
                    remainingCapacity -= info.getSize();
                    contentItr.remove();
                }
            }
        }

        if (!libCapacities.isEmpty()) {
            throw new InsufficientSpaceException("Could not fit selected content in device");
        }

        var transferToLibrary = new LinkedList<String>();
        for (var onDevice : attachedDevice.getContent().keySet()) {
            if (!desiredDeviceContent.contains(onDevice)) {
                if (!mainLibrary.containsContent(onDevice)) {
                    switch (strategy) {
                        case TRANSFER_TO_LIBRARY:
                            transferToLibrary.add(onDevice);
                            break;
                        case DELETE_FROM_DEVICE:
                            plan.deleteFromDevice.add(onDevice);
                            break;
                        case CANCEL_SYNC:
                            throw new SyncStrategyException("Content to be removed from device is not in the library");
                    }
                } else {
                    plan.deleteFromDevice.add(onDevice);
                }
            }
        }

        for (var contentDestination : contentDestinations.entrySet()) {
            var contentId = contentDestination.getKey();
            var destination = contentDestination.getValue();

            var syncStatus = syncStatuses.get(contentId);
            if (syncStatus.isOnDevice()) {
                var info = attachedDevice.getContent(contentId);
                if (!info.getLibraryId().equals(destination)) {
                    plan.transferOnDevice.put(contentId, destination);
                }
            } else {
                plan.transferToDevice.put(contentId, destination);
            }
        }

        // TODO handle transfer to library

        return plan;
    }
}
