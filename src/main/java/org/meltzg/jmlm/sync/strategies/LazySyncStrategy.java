package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.util.*;
import java.util.stream.Collectors;

public class LazySyncStrategy extends AbstractSyncStrategy {

    @Override
    protected void planDeviceTransfers(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses, Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException {
        var libFreeSpaces = new HashMap<UUID, Long>();

        for (var toDeleteId : plan.getDeleteFromDevice()) {
            var contentInfo = syncStatuses.get(toDeleteId).getContentInfo();
            var contentLib = syncStatuses.get(toDeleteId).getDeviceLibrary();
            libFreeSpaces.put(contentLib,
                    libFreeSpaces.getOrDefault(contentLib, 0L) + contentInfo.getSize());
        }
        for (var initialFreeSpaceEntry : destinationLibFreeSpace.entrySet()) {
            var libId = initialFreeSpaceEntry.getKey();
            long freeSpace = initialFreeSpaceEntry.getValue();
            libFreeSpaces.put(initialFreeSpaceEntry.getKey(),
                    libFreeSpaces.getOrDefault(libId, 0L) + freeSpace);
        }

        var sortedDesiredContent = new ArrayList<>(desiredContentInfo);
        sortedDesiredContent.sort(
                (info1, info2) -> Long.signum(info2.getSize() - info1.getSize()));
        var freeSpaceEntries = libFreeSpaces.entrySet().stream()
                .sorted((lib1, lib2) -> Long.signum(lib2.getValue() - lib1.getValue()))
                .collect(Collectors.toList());

        var desiredContentItr = sortedDesiredContent.iterator();
        while (desiredContentItr.hasNext()) {
            var desiredContent = desiredContentItr.next();
            if (syncStatuses.get(desiredContent.getId()).isOnDevice()) {
                desiredContentItr.remove();
                continue;
            }
            var spaceFound = false;
            for (var freeSpaceEntry : freeSpaceEntries) {
                var freeSpace = freeSpaceEntry.getValue();
                if (desiredContent.getSize() <= freeSpace) {
                    plan.getTransferToDevice().put(desiredContent.getId(), freeSpaceEntry.getKey());
                    spaceFound = true;
                    desiredContentItr.remove();
                    freeSpaceEntry.setValue(freeSpaceEntry.getValue() - desiredContent.getSize());
                    break;
                }
            }
            if (!spaceFound) {
                throw new InsufficientSpaceException(
                        getClass().getCanonicalName() + ": Could not fit content " + desiredContent.getId() + " in device");
            }
        }
    }
}
