package org.meltzg.jmlm.sync;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.sync.strategies.GreedySyncStrategy;
import org.meltzg.jmlm.sync.strategies.LazySyncStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.meltzg.jmlm.CommonUtil.RESOURCEDIR;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DeviceSyncManagerTest {
    static FileSystemAudioContentDevice device1;
    static FileSystemAudioContentDevice device2;
    @Autowired
    AudioContentRepository contentRepo;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        device1 = new FileSystemAudioContentDevice(contentRepo);
        device2 = new FileSystemAudioContentDevice(contentRepo);

        device1.addLibraryRoot(RESOURCEDIR + "/audio/jst2018-12-09");
        device2.addLibraryRoot(RESOURCEDIR + "/audio/kwgg2016-10-29");
    }

    @Test
    public void testRefreshSyncStatus() throws FileNotFoundException {
        var syncManager = new DeviceSyncManager(device1, device2, null);

        syncManager.refreshSyncStatus();

        assertEquals(device1.getContent().size() + device2.getContent().size(),
                syncManager.getSyncStatuses().size());
        for (var id : device1.getContent().keySet()) {
            var syncStatus = syncManager.getSyncStatus(id);
            assertEquals(id, syncStatus.getContentId());
            assertTrue(syncStatus.isInLibrary());
            assertFalse(syncStatus.isOnDevice());
        }
        for (var id : device2.getContent().keySet()) {
            var syncStatus = syncManager.getSyncStatus(id);
            assertEquals(id, syncStatus.getContentId());
            assertFalse(syncStatus.isInLibrary());
            assertTrue(syncStatus.isOnDevice());
        }
    }

    @Test
    public void testGetSyncStatusNotOnDevice() {
        var syncManager = new DeviceSyncManager(device1, device2, null);

        var status = syncManager.getSyncStatus("not there");
        assertFalse(status.isInLibrary());
        assertFalse(status.isOnDevice());
    }

    @Test(expected = SyncStrategyException.class)
    public void testCreateSyncPlanClearDeviceCancelSync() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test
    public void testCreateSyncPlanClearDeviceTransferToLibrary() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        var plan = syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.TRANSFER_TO_LIBRARY);

        assertEquals(plan.getTransferToDevice().size(), device1.getContent().size());
        assertEquals(plan.getTransferOnDevice().size(), 0);
        assertTrue(plan.getDeleteFromDevice().containsAll(device2.getContent().keySet()));
        assertEquals(plan.getTransferToLibrary().keySet(), device2.getContent().keySet());
        assertEquals(plan.getTransferOnLibrary().size(), 0);
        assertEquals(plan.getDeleteFromLibrary().size(), 0);
    }

    @Test
    public void testCreateSyncPlanClearDeviceDeleteContent() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        var plan = syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.DELETE_FROM_DEVICE);

        assertEquals(plan.getTransferToDevice().size(), device1.getContent().size());
        assertEquals(plan.getTransferOnDevice().size(), 0);
        assertTrue(plan.getDeleteFromDevice().containsAll(device2.getContent().keySet()));
        assertEquals(plan.getTransferToLibrary().size(), 0);
        assertEquals(plan.getTransferOnLibrary().size(), 0);
        assertEquals(plan.getDeleteFromLibrary().size(), 0);
    }

    private List<String> getRankedStrategies() {
        return Arrays.asList(LazySyncStrategy.class.getCanonicalName(),
                GreedySyncStrategy.class.getCanonicalName());
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSyncPlanNoLibraryLibraries() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = new FileSystemAudioContentDevice(contentRepo);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSyncPlanClearDeviceNoDeviceLibraries() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device2 = new FileSystemAudioContentDevice(contentRepo);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test(expected = FileNotFoundException.class)
    public void testCreateSyncPlanClearDeviceContentNotFound() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(new HashSet<>(Collections.singletonList("notOnDevice")),
                NotInLibraryStrategy.CANCEL_SYNC);
    }
}