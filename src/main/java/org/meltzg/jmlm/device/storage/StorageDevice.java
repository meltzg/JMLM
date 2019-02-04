package org.meltzg.jmlm.device.storage;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
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
