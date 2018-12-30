package org.meltzg.jmlm.sync;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.*;
import static org.meltzg.jmlm.CommonUtil.*;

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
}