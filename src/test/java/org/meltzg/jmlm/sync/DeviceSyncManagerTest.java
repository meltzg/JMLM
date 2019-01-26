package org.meltzg.jmlm.sync;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.sync.strategies.GreedySyncStrategy;
import org.meltzg.jmlm.sync.strategies.LazySyncStrategy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static org.junit.Assert.*;
import static org.meltzg.jmlm.CommonUtil.RESOURCEDIR;

public class DeviceSyncManagerTest {
    private Gson gson;

    @Before
    public void setUp() {
        this.gson = new FileSystemAudioContentDevice().getGson();
    }

    @Test
    public void testRefreshSyncStatus() throws FileNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/audio/jst2018-12-09.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/audio/kwgg2016-10-29.json"),
                FileSystemAudioContentDevice.class);

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
    public void testGetSyncStatusNotOnDevice() throws FileNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/audio/jst2018-12-09.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/audio/kwgg2016-10-29.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, null);

        var status = syncManager.getSyncStatus("not there");
        assertFalse(status.isInLibrary());
        assertFalse(status.isOnDevice());
    }

    @Test
    public void testCreateSyncPlan() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/empty-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        var plan = syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);

        assertEquals(plan.transferToDevice.keySet(), device1.getContent().keySet());
        assertEquals(plan.transferOnDevice.size(), 0);
        assertEquals(plan.deleteFromDevice.size(), 0);
        assertEquals(plan.transferToLibrary.size(), 0);
        assertEquals(plan.transferOnLibrary.size(), 0);
        assertEquals(plan.deleteFromLibrary.size(), 0);
    }

    @Test(expected = SyncStrategyException.class)
    public void testCreateSyncPlanClearDeviceCancelSync() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/empty-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test
    public void testCreateSyncPlanClearDeviceTransferToLibrary() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/empty-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        var plan = syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.TRANSFER_TO_LIBRARY);

        assertEquals(plan.transferToDevice.size(), 0);
        assertEquals(plan.transferOnDevice.size(), 0);
        assertTrue(plan.deleteFromDevice.containsAll(device2.getContent().keySet()));
        assertEquals(plan.transferToLibrary.keySet(), device2.getContent().keySet());
        assertEquals(plan.transferOnLibrary.size(), 0);
        assertEquals(plan.deleteFromLibrary.size(), 0);
    }

    @Test
    public void testCreateSyncPlanClearDeviceDeleteContent() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/empty-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        var plan = syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.DELETE_FROM_DEVICE);

        assertEquals(plan.transferToDevice.size(), 0);
        assertEquals(plan.transferOnDevice.size(), 0);
        assertTrue(plan.deleteFromDevice.containsAll(device2.getContent().keySet()));
        assertEquals(plan.transferToLibrary.size(), 0);
        assertEquals(plan.transferOnLibrary.size(), 0);
        assertEquals(plan.deleteFromLibrary.size(), 0);
    }

    private List<String> getRankedStrategies() {
        return Arrays.asList(LazySyncStrategy.class.getCanonicalName(),
                GreedySyncStrategy.class.getCanonicalName());
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSyncPlanNoLibraryLibraries() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = new FileSystemAudioContentDevice();
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateSyncPlanClearDeviceNoDeviceLibraries() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = new FileSystemAudioContentDevice();

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(device1.getContent().keySet(),
                NotInLibraryStrategy.CANCEL_SYNC);
    }

    @Test(expected = FileNotFoundException.class)
    public void testCreateSyncPlanClearDeviceContentNotFound() throws FileNotFoundException, InsufficientSpaceException, SyncStrategyException, ClassNotFoundException {
        var device1 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/full-device.json"),
                FileSystemAudioContentDevice.class);
        var device2 = gson.fromJson(new FileReader(RESOURCEDIR + "/mocks/empty-device.json"),
                FileSystemAudioContentDevice.class);

        var syncManager = new DeviceSyncManager(device1, device2, getRankedStrategies());
        syncManager.createSyncPlan(new HashSet<>(Collections.singletonList("notOnDevice")),
                NotInLibraryStrategy.CANCEL_SYNC);
    }
}