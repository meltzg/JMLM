package org.meltzg.jmlm.sync.strategies;

import org.junit.Before;
import org.junit.Test;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.sync.ContentSyncStatus;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractSyncStrategyTest {

    private AbstractSyncStrategy strategy;

    private UUID libId1;
    private UUID libId2;
    private Map<UUID, Long> libCapacities;
    private Map<UUID, Long> libFreespace;
    private List<AudioContent> desiredContentInfo;
    private Map<String, ContentSyncStatus> syncStatuses;

    @Before
    public void setUp() throws ClassNotFoundException {
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

        this.strategy = createStrategy();
    }

    @Test
    public void createPlanEmptyDevice() throws InsufficientSpaceException {
        for (var i = 0; i < 4; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setSize(25);
            desiredContentInfo.add(content);

            syncStatuses.put(content.getId(), new ContentSyncStatus(
                    content,
                    UUID.randomUUID(),
                    null
            ));
        }

        var plan = strategy.createPlan(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
        assertEquals(desiredContentInfo.size(), plan.getTransferToDevice().size());
        assertTrue(plan.getTransferToDevice().keySet().containsAll(desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toList())));
    }

    @Test(expected = InsufficientSpaceException.class)
    public void createPlanInsufficientSpace() throws InsufficientSpaceException {
        for (var i = 0; i < 5; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setSize(25);
            desiredContentInfo.add(content);

            syncStatuses.put(content.getId(), new ContentSyncStatus(
                    content,
                    UUID.randomUUID(),
                    null
            ));
        }

        strategy.createPlan(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
    }

    @Test
    public void createPlanNeedsShuffle() throws InsufficientSpaceException {
        var contents = new ArrayList<AudioContent>();
        for (var i = 0; i < 3; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setSize(25);
            contents.add(content);
            desiredContentInfo.add(content);
        }

        libFreespace.put(libId1, libFreespace.get(libId1) - contents.get(0).getSize());
        syncStatuses.put(contents.get(0).getId(), new ContentSyncStatus(
                contents.get(0),
                UUID.randomUUID(),
                libId1
        ));
        libFreespace.put(libId2, libFreespace.get(libId2) - contents.get(1).getSize());
        syncStatuses.put(contents.get(1).getId(), new ContentSyncStatus(
                contents.get(1),
                UUID.randomUUID(),
                libId2
        ));
        contents.get(2).setSize(50);
        syncStatuses.put(contents.get(2).getId(), new ContentSyncStatus(
                contents.get(2),
                UUID.randomUUID(),
                null
        ));

        var plan = strategy.createPlan(desiredContentInfo, syncStatuses, libCapacities, libFreespace);
        assertEquals(1, plan.getTransferOnDevice().size());
        assertEquals(1, plan.getTransferToDevice().size());
        assertTrue(Arrays.asList(contents.get(0).getId(), contents.get(1).getId()).contains(
                plan.getTransferOnDevice().keySet().iterator().next()));
        assertTrue(plan.getTransferToDevice().keySet().contains(contents.get(2).getId()));
    }

    @Test
    public void createPlanNeedsDeleteContent() throws InsufficientSpaceException {
        for (var i = 0; i < 4; i++) {
            var content = new AudioContent();
            content.setId(UUID.randomUUID().toString());
            content.setSize(25);
            desiredContentInfo.add(content);

            syncStatuses.put(content.getId(), new ContentSyncStatus(
                    content,
                    UUID.randomUUID(),
                    null
            ));
        }

        var existingContent = new AudioContent();
        existingContent.setId(UUID.randomUUID().toString());
        existingContent.setSize(25);
        libFreespace.put(libId1, libFreespace.get(libId1) - existingContent.getSize());
        syncStatuses.put(existingContent.getId(), new ContentSyncStatus(
                existingContent,
                UUID.randomUUID(),
                UUID.randomUUID()
        ));


        var plan = strategy.createPlan(desiredContentInfo, syncStatuses, libCapacities, libFreespace);

        assertEquals(desiredContentInfo.size(), plan.getTransferToDevice().size());
        assertTrue(plan.getTransferToDevice().keySet().containsAll(desiredContentInfo.stream()
                .map(AudioContent::getId)
                .collect(Collectors.toList())));
        assertTrue(plan.getDeleteFromDevice().contains(existingContent.getId()));
    }

    protected abstract AbstractSyncStrategy createStrategy() throws ClassNotFoundException;
}