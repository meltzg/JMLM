package org.meltzg.jmlm.sync;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.sync.strategies.RankedSyncStrategy;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DeviceSyncManager {
    private FileSystemAudioContentDevice mainLibrary;
    private FileSystemAudioContentDevice attachedDevice;
    private Map<String, ContentSyncStatus> syncStatuses;
    private Map<String, AudioContent> allContent;
    private List<String> rankedStrategyClassNames;


    public DeviceSyncManager(FileSystemAudioContentDevice mainLibrary, FileSystemAudioContentDevice attachedDevice,
                             List<String> rankedStrategyClassNames) {
        this.mainLibrary = mainLibrary;
        this.attachedDevice = attachedDevice;
        this.syncStatuses = null;
        this.allContent = null;
        this.rankedStrategyClassNames = rankedStrategyClassNames;

        refreshSyncStatus();
    }

    public void refreshSyncStatus() {
        syncStatuses = new HashMap<>();
        allContent = new HashMap<>();

        allContent.putAll(mainLibrary.getContent());
        allContent.putAll(attachedDevice.getContent());

        for (var contentInfo : allContent.values()) {
            var id = contentInfo.getId();
            var mainLibraryId = mainLibrary.containsContent(id) ? mainLibrary.getContent(id).getLibraryId() : null;
            var deviceLibraryId = attachedDevice.containsContent(id) ? attachedDevice.getContent(id).getLibraryId() : null;
            var syncStatus = new ContentSyncStatus(contentInfo, mainLibraryId, deviceLibraryId);
            syncStatuses.put(id, syncStatus);
        }
    }


    public Map<String, ContentSyncStatus> getSyncStatuses() {
        return syncStatuses;
    }

    public ContentSyncStatus getSyncStatus(String id) {
        var status = this.syncStatuses.get(id);
        if (status == null) {
            AudioContent contentInfo = new AudioContent();
            contentInfo.setId(id);
            status = new ContentSyncStatus(contentInfo, null, null);
        }
        return status;
    }

    public void setRankedStrategies(List<String> rankedStrategyClassNames) {
        this.rankedStrategyClassNames = rankedStrategyClassNames;
    }

    public SyncPlan createSyncPlan(Set<String> desiredContent, NotInLibraryStrategy notInLibraryStrategy) throws FileNotFoundException, SyncStrategyException, InsufficientSpaceException, ClassNotFoundException {
        if (mainLibrary.getLibraryRoots().isEmpty()) {
            throw new IllegalStateException("Main library device has no libraries configured");
        }
        if (attachedDevice.getLibraryRoots().isEmpty()) {
            throw new IllegalStateException("Attached device has no libraries configured");
        }

        var desiredContentInfo = desiredContent.stream().map(this::getContentInfo).collect(Collectors.toList());
        if (desiredContentInfo.stream().anyMatch(Objects::isNull)) {
            throw new FileNotFoundException("Content is not in the library or the attached device");

        }

        var strategy = new RankedSyncStrategy(rankedStrategyClassNames.toArray(new String[0]));

        var plan = strategy.createPlan(desiredContentInfo, syncStatuses,
                attachedDevice.getLibraryRootCapacities(), attachedDevice.getLibraryRootFreeSpace());

        var transferToLibrary = new LinkedList<String>();
        for (var toDelete : plan.deleteFromDevice) {
            if (!syncStatuses.get(toDelete).isInLibrary()) {
                switch (notInLibraryStrategy) {
                    case TRANSFER_TO_LIBRARY:
                        transferToLibrary.add(toDelete);
                        break;
                    case DELETE_FROM_DEVICE:
                        break;
                    case CANCEL_SYNC:
                        throw new SyncStrategyException("Content to be removed from device is not in the library");
                }
            }
        }

        // handle transferToLibrary as a sync
        if (!transferToLibrary.isEmpty()) {
            var desiredLibContent = transferToLibrary.stream()
                    .map(contentId -> allContent.get(contentId))
                    .collect(Collectors.toList());
            desiredLibContent.addAll(mainLibrary.getContent().values());
            var reverseSyncStatuses = syncStatuses.values().stream()
                    .map(ContentSyncStatus::reverse)
                    .collect(Collectors.toMap(ContentSyncStatus::getContentId, Function.identity()));
            var libPlan = strategy.createPlan(desiredLibContent, reverseSyncStatuses,
                    mainLibrary.getLibraryRootCapacities(), mainLibrary.getLibraryRootFreeSpace());

            plan.transferToLibrary = libPlan.transferToDevice;
            plan.transferOnLibrary = libPlan.transferOnDevice;
            plan.deleteFromLibrary = libPlan.deleteFromDevice;
        }

        return plan;
    }

    private AudioContent getContentInfo(String contentId) {
        var syncStatus = syncStatuses.get(contentId);
        if (syncStatus != null) {
            if (syncStatus.isInLibrary()) {
                return mainLibrary.getContent(contentId);
            }
            if (syncStatus.isOnDevice()) {
                return attachedDevice.getContent(contentId);
            }
        }
        return null;
    }
}
