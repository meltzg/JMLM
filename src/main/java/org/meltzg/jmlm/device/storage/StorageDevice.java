package org.meltzg.jmlm.device.storage;

import java.util.Objects;

public class StorageDevice {
    private String id;
    private long capacity;
    private int partitions;

    public StorageDevice(String id, long capacity, int partitions) {
        this.id = id;
        this.capacity = capacity;
        this.partitions = partitions;
    }

    private StorageDevice() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageDevice that = (StorageDevice) o;
        return getCapacity() == that.getCapacity() &&
                getPartitions() == that.getPartitions() &&
                getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCapacity(), getPartitions());
    }
}
