package org.meltzg.jmlm.device.storage;


import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageDevice {
    @Id
    private String id;
    private String storageId;
    private long capacity;
    private long freeSpace;
    private int partitions;

    public StorageDevice(String storageId, long capacity, long freeSpace, int partitions) {
        id = UUID.randomUUID().toString();
        this.storageId = storageId;
        this.capacity = capacity;
        this.freeSpace = freeSpace;
        this.partitions = partitions;
    }
}
