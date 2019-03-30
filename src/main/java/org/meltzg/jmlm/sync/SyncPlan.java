package org.meltzg.jmlm.sync;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
@Data
public class SyncPlan {
    private Map<Long, UUID> transferToDevice = new HashMap<>();
    private Map<Long, UUID> transferOnDevice = new HashMap<>();
    private List<Long> deleteFromDevice = new ArrayList<>();

    private Map<Long, UUID> transferToLibrary = new HashMap<>();
    private Map<Long, UUID> transferOnLibrary = new HashMap<>();
    private List<Long> deleteFromLibrary = new ArrayList<>();
}
