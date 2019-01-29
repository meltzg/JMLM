package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;
import org.meltzg.jmlm.sync.SyncPlan;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractSyncStrategy {
    protected SyncPlan plan;

    public SyncPlan createPlan(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses,
                               Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException {
        plan = new SyncPlan();

        planContentToRemove(desiredContentInfo, syncStatuses);
        planDeviceTransfers(desiredContentInfo, syncStatuses, destinationLibCapacities, destinationLibFreeSpace);

        return plan;
    }

    protected abstract void planDeviceTransfers(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses,
                                                Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException;

    private void planContentToRemove(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses) {
        var desiredContentIds = desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toSet());
        var contentToRemove = syncStatuses.values().stream()
                .filter((syncStatus) -> syncStatus.isOnDevice() &&
                        !desiredContentIds.contains(syncStatus.getContentId()))
                .map(ContentSyncStatus::getContentId)
                .collect(Collectors.toList());
        plan.deleteFromDevice.addAll(contentToRemove);
    }
}
