package org.meltzg.jmlm.device.storage;

import java.math.BigInteger;

public class StorageDevice {
    private String id;
    private BigInteger capacity;

    public StorageDevice(String id, BigInteger capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigInteger getCapacity() {
        return capacity;
    }

    public void setCapacity(BigInteger capacity) {
        this.capacity = capacity;
    }
}
