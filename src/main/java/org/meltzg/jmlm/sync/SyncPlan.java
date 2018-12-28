package org.meltzg.jmlm.sync;

import java.util.*;

public class SyncPlan {
    public Map<String, UUID> transferToDevice;
    public List<String> deleteFromDevice;
    public Map<String, UUID> transferOnDevice;
    public Map<String, UUID> transferToLibrary;

    public SyncPlan() {
        this.transferToDevice = new HashMap<>();
        this.deleteFromDevice = new ArrayList<>();
        this.transferOnDevice = new HashMap<>();
        this.transferToLibrary = new HashMap<>();
    }
}
