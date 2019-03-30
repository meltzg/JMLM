package org.meltzg.jmlm.sync;

import lombok.Getter;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.sync.strategies.RankedSyncStrategy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceSyncManager {
    private FileSystemAudioContentDevice mainLibrary;
    private FileSystemAudioContentDevice attachedDevice;
    @Getter
    private Map<Long, ContentSyncStatus> syncStatuses;
    private Map<Long, AudioContent> allContent;
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
            var mainContentLocation = mainLibrary.getContentLocations().get(id);
            var attachedContentLocation = attachedDevice.getContentLocations().get(id);
            var mainLibraryId = mainLibrary.containsContent(id) ? mainContentLocation.getLibraryId() : null;
            var deviceLibraryId = attachedDevice.containsContent(id) ? attachedContentLocation.getLibraryId() : null;
            var syncStatus = new ContentSyncStatus(contentInfo, mainLibraryId, deviceLibraryId);
            syncStatuses.put(id, syncStatus);
        }
    }

    public ContentSyncStatus getSyncStatus(Long id) {
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

    public SyncPlan createSyncPlan(Set<Long> desiredContent, NotInLibraryStrategy notInLibraryStrategy) throws FileNotFoundException, SyncStrategyException, InsufficientSpaceException, ClassNotFoundException {
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

        var transferToLibrary = new LinkedList<Long>();
        for (var toDelete : plan.getDeleteFromDevice()) {
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

            plan.setTransferToLibrary(libPlan.getTransferToDevice());
            plan.setTransferOnLibrary(libPlan.getTransferOnDevice());
            plan.setDeleteFromLibrary(libPlan.getDeleteFromDevice());
        }

        return plan;
    }

    public void syncDevice(Set<Long> desiredContent, NotInLibraryStrategy notInLibraryStrategy) throws ClassNotFoundException, IOException, InsufficientSpaceException, SyncStrategyException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException {
        var plan = createSyncPlan(desiredContent, notInLibraryStrategy);
        syncDevice(plan);
    }

    public void syncDevice(SyncPlan plan) throws IOException, ReadOnlyFileException, TagException, InvalidAudioFrameException, CannotReadException {
        for (var toDelete : plan.getDeleteFromLibrary()) {
            mainLibrary.deleteContent(toDelete);
        }
        for (var transferOn : plan.getTransferOnLibrary().entrySet()) {
            mainLibrary.moveContent(transferOn.getKey(), transferOn.getValue());
        }
        for (var transferTo : plan.getTransferToLibrary().entrySet()) {
            var contentLocation = attachedDevice.getContentLocations().get(transferTo.getKey());
            mainLibrary.addContent(attachedDevice.getContentStream(transferTo.getKey()),
                    contentLocation.getLibrarySubPath(),
                    transferTo.getValue());
        }


        for (var toDelete : plan.getDeleteFromDevice()) {
            attachedDevice.deleteContent(toDelete);
        }
        for (var transferOn : plan.getTransferOnDevice().entrySet()) {
            attachedDevice.moveContent(transferOn.getKey(), transferOn.getValue());
        }
        for (var transferTo : plan.getTransferToDevice().entrySet()) {
            var contentLocation = mainLibrary.getContentLocations().get(transferTo.getKey());
            attachedDevice.addContent(mainLibrary.getContentStream(transferTo.getKey()),
                    contentLocation.getLibrarySubPath(),
                    transferTo.getValue());
        }
    }


    private AudioContent getContentInfo(Long contentId) {
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
