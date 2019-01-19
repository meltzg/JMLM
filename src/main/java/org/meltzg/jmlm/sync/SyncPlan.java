package org.meltzg.jmlm.sync;

import java.util.*;

public class SyncPlan {
    public Map<String, UUID> transferToDevice;
    public Map<String, UUID> transferOnDevice;
    public List<String> deleteFromDevice;

    public Map<String, UUID> transferToLibrary;
    public Map<String, UUID> transferOnLibrary;
    public List<String> deleteFromLibrary;


    public SyncPlan() {
        this.transferToDevice = new HashMap<>();
        this.transferOnDevice = new HashMap<>();
        this.deleteFromDevice = new ArrayList<>();

        this.transferToLibrary = new HashMap<>();
        this.transferOnLibrary = new HashMap<>();
        this.deleteFromLibrary = new ArrayList<>();
    }
}
