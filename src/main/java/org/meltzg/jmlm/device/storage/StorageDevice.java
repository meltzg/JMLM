package org.meltzg.jmlm.device.storage;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageDevice {
    @Id
    private String id;
    private long capacity;
    private long freeSpace;
    private int partitions;
}
