package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.util.*;
import java.util.stream.Collectors;

public class GreedySyncStrategy extends AbstractSyncStrategy {
    @Override
    protected void planDeviceTransfers(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses,
                                       Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException {
        var libCapacities = destinationLibCapacities.entrySet()
                .stream()
                .sorted((lib1, lib2) -> Long.signum(lib2.getValue() - lib1.getValue()))
                .collect(Collectors.toList());

        var sortedDesiredContent = new ArrayList<>(desiredContentInfo);
        sortedDesiredContent.sort(
                (info1, info2) -> Long.signum(info2.getSize() - info1.getSize()));

        var contentDestinations = new HashMap<String, UUID>();
        for (var lib : libCapacities) {
            var libId = lib.getKey();
            long remainingCapacity = lib.getValue();
            var contentItr = sortedDesiredContent.iterator();
            while (contentItr.hasNext()) {
                var info = contentItr.next();
                if (info.getSize() <= remainingCapacity) {
                    contentDestinations.put(info.getCrossDeviceId(), libId);
                    remainingCapacity -= info.getSize();
                    contentItr.remove();
                }
            }
        }

        if (!sortedDesiredContent.isEmpty()) {
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
    }
}
