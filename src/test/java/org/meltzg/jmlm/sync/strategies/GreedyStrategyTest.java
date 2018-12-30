package org.meltzg.jmlm.sync.strategies;

import org.junit.Before;
import org.junit.Test;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.util.*;

import static org.junit.Assert.*;

public class GreedyStrategyTest {

    private UUID libId1;
    private UUID libId2;
    private Map<UUID, Long> libCapacities;
    private Map<UUID, Long> libFreespace;
    private List<AudioContent> desiredContentInfo;
    private Map<String, ContentSyncStatus> syncStatuses;

    @Before
    public void setUp() {
        libId1 = UUID.randomUUID();
        libId2 = UUID.randomUUID();
        libCapacities = new HashMap<>();
        libFreespace = new HashMap<>();
        desiredContentInfo = new ArrayList<>();
        syncStatuses = new HashMap<>();

        libCapacities.put(libId1, 50L);
        libCapacities.put(libId2, 50L);
        libCapacities.put(libId1, 50L);
        libCapacities.put(libId2, 50L);

        libFreespace.put(libId1, 50L);
        libFreespace.put(libId2, 50L);
        libFreespace.put(libId1, 50L);
        libFreespace.put(libId2, 50L);
    }

    @Test
    public void createStrategyEmptyDevice() throws InsufficientSpaceException {
        for (var i = 0; i < 4; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setLibraryId(UUID.randomUUID());
            content.setSize(25);
            desiredContentInfo.add(content);

            syncStatuses.put(content.getId(), new ContentSyncStatus(
                    content.getId(),
                    content.getLibraryId(),
                    null
            ));
        }

        var plan = new GreedyStrategy().createStrategy(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
        assertEquals(desiredContentInfo.size(), plan.transferToDevice.size());
    }
}