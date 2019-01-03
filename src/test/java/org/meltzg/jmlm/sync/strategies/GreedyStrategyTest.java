package org.meltzg.jmlm.sync.strategies;

import org.junit.Before;
import org.junit.Test;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.util.*;
import java.util.stream.Collectors;

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
        assertTrue(plan.transferToDevice.keySet().containsAll(desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toList())));
    }

    @Test(expected = InsufficientSpaceException.class)
    public void createStrategyInsufficientSpace() throws InsufficientSpaceException {
        for (var i = 0; i < 5; i++) {
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

        new GreedyStrategy().createStrategy(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
    }

    @Test
    public void createStrategyNeedsShuffle() throws InsufficientSpaceException {
        var contents = new ArrayList<AudioContent>();
        for (var i = 0; i < 3; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setLibraryId(UUID.randomUUID());
            content.setSize(25);
            contents.add(content);
            desiredContentInfo.add(content);
        }

        libFreespace.put(libId1, libFreespace.get(libId1) - contents.get(0).getSize());
        syncStatuses.put(contents.get(0).getId(), new ContentSyncStatus(
                contents.get(0).getId(),
                contents.get(0).getLibraryId(),
                libId1
        ));
        libFreespace.put(libId2, libFreespace.get(libId2) - contents.get(1).getSize());
        syncStatuses.put(contents.get(1).getId(), new ContentSyncStatus(
                contents.get(1).getId(),
                contents.get(1).getLibraryId(),
                libId2
        ));
        contents.get(2).setSize(50);
        syncStatuses.put(contents.get(2).getId(), new ContentSyncStatus(
                contents.get(2).getId(),
                contents.get(2).getLibraryId(),
                null
        ));

        var plan = new GreedyStrategy().createStrategy(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
        assertEquals(1, plan.transferOnDevice.size());
        assertEquals(1, plan.transferToDevice.size());
        assertTrue(Arrays.asList(contents.get(0).getId(), contents.get(1).getId()).contains(
                plan.transferOnDevice.keySet().iterator().next()));
        assertTrue(plan.transferToDevice.keySet().contains(contents.get(2).getId()));
    }

    @Test
    public void createStrategyNeedsDeleteContent() throws InsufficientSpaceException {
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

        var existingContent = new AudioContent();
        existingContent.setId(UUID.randomUUID().toString());
        existingContent.setLibraryId(libId1);
        existingContent.setSize(25);
        libFreespace.put(libId1, libFreespace.get(libId1) - existingContent.getSize());
        syncStatuses.put(existingContent.getId(), new ContentSyncStatus(
                existingContent.getId(),
                UUID.randomUUID(),
                existingContent.getLibraryId()
        ));


        var plan = new GreedyStrategy().createStrategy(desiredContentInfo, syncStatuses, libCapacities, libFreespace);

        assertEquals(desiredContentInfo.size(), plan.transferToDevice.size());
        assertTrue(plan.transferToDevice.keySet().containsAll(desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toList())));
        assertTrue(plan.deleteFromDevice.contains(existingContent.getId()));
    }
}