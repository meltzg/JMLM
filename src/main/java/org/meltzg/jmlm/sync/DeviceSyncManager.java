package org.meltzg.jmlm.sync;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.sync.strategies.ISyncStrategy;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class DeviceSyncManager {
    private FileSystemAudioContentDevice mainLibrary;
    private FileSystemAudioContentDevice attachedDevice;
    private Map<String, ContentSyncStatus> syncStatuses;
    private Set<String> allContent;
    private List<Class<? extends ISyncStrategy>> rankedStrategies;


    public DeviceSyncManager(FileSystemAudioContentDevice mainLibrary, FileSystemAudioContentDevice attachedDevice,
                             List<Class<? extends ISyncStrategy>> rankedStrategies) {
        this.mainLibrary = mainLibrary;
        this.attachedDevice = attachedDevice;
        this.syncStatuses = null;
        this.allContent = null;
        this.rankedStrategies = rankedStrategies;

        refreshSyncStatus();
    }

    public void refreshSyncStatus() {
        syncStatuses = new HashMap<>();
        allContent = new HashSet<>();

        allContent.addAll(mainLibrary.getContent().keySet());
        allContent.addAll(attachedDevice.getContent().keySet());

        for (var id : allContent) {
            var mainLibraryId = mainLibrary.containsContent(id) ? mainLibrary.getContent(id).getLibraryId() : null;
            var deviceLibraryId = attachedDevice.containsContent(id) ? attachedDevice.getContent(id).getLibraryId() : null;
            var syncStatus = new ContentSyncStatus(id, mainLibraryId, deviceLibraryId);
            syncStatuses.put(id, syncStatus);
        }
    }


    public Map<String, ContentSyncStatus> getSyncStatuses() {
        return syncStatuses;
    }

    public ContentSyncStatus getSyncStatus(String id) {
        var status = this.syncStatuses.get(id);
        if (status == null) {
            status = new ContentSyncStatus(id, null, null);
        }
        return status;
    }

    public void setRankedStrategies(List<Class<? extends ISyncStrategy>> rankedStrategies) {
        this.rankedStrategies = rankedStrategies;
    }

    public SyncPlan createSyncPlan(Set<String> desiredContent, NotInLibraryStrategy notInLibraryStrategy) throws FileNotFoundException, SyncStrategyException, InsufficientSpaceException {
        SyncPlan plan = null;

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

        for (var clazz : rankedStrategies) {
            try {
                var strategy = clazz.getDeclaredConstructor().newInstance();
                try {
                    plan = strategy.createStrategy(desiredContentInfo, syncStatuses,
                            attachedDevice.getLibraryRootCapacities(), attachedDevice.getLibraryRootFreeSpace());
                    break;
                } catch (InsufficientSpaceException e) {
                    e.printStackTrace();
                    plan = null;
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        if (plan == null) {
            throw new InsufficientSpaceException("Could not fit selected content in device using any strategy");
        }

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

        // TODO handle transferToLibrary

        return plan;
    }

    private AudioContent getContentInfo(String contentId) {
        var syncStatus = syncStatuses.get(contentId);
        if (syncStatus.isInLibrary()) {
            return mainLibrary.getContent(contentId);
        }
        if (syncStatus.isOnDevice()) {
            return attachedDevice.getContent(contentId);
        }
        return null;
    }
}
