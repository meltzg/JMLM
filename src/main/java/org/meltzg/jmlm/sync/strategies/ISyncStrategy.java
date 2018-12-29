package org.meltzg.jmlm.sync.strategies;

import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;
import org.meltzg.jmlm.sync.SyncPlan;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ISyncStrategy {
    SyncPlan createStrategy(List<AudioContent> desiredContentInfo, Map<String, ContentSyncStatus> syncStatuses,
                            Map<UUID, Long> destinationLibCapacities, Map<UUID, Long> destinationLibFreeSpace) throws InsufficientSpaceException;
}
