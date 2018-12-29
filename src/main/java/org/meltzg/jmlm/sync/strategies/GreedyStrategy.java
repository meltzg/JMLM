package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;
import org.meltzg.jmlm.sync.SyncPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GreedyStrategy implements ISyncStrategy {
    @Override
    public SyncPlan createStrategy(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses,
                                   Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException {
        var plan = new SyncPlan();

        var libCapacities = destinationLibCapacities.entrySet()
                .stream()
                .sorted((lib1, lib2) -> Long.signum(lib2.getValue() - lib1.getValue()))
                .collect(Collectors.toList());
        desiredContentInfo.sort((info1, info2) -> Long.signum(info2.getSize() - info1.getSize()));

        var contentDestinations = new HashMap<String, UUID>();
        for (var lib : libCapacities) {
            var libId = lib.getKey();
            var remainingCapacity = lib.getValue();
            var contentItr = desiredContentInfo.iterator();
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
            throw new InsufficientSpaceException(
                    getClass().getCanonicalName() + ": Could not fit selected content in device");
        }

        for (var contentDestination : contentDestinations.entrySet()) {
            var contentId = contentDestination.getKey();
            var destination = contentDestination.getValue();

            var syncStatus = syncStatuses.get(contentId);
            if (syncStatus.isOnDevice()) {
                if (syncStatus.getDeviceLibrary().equals(destination)) {
                    plan.transferOnDevice.put(contentId, destination);
                }
            } else {
                plan.transferToDevice.put(contentId, destination);
            }
        }

        var desiredContentIds = desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toSet());
        var contentToRemove = syncStatuses.values().stream()
                .filter((syncStatus) -> syncStatus.isOnDevice() &&
                        !desiredContentIds.contains(syncStatus.getContentId()))
                .map(ContentSyncStatus::getContentId)
                .collect(Collectors.toList());

        plan.deleteFromDevice.addAll(contentToRemove);

        return plan;
    }
}
