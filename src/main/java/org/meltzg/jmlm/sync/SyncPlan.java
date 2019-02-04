package org.meltzg.jmlm.sync;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
@Data
public class SyncPlan {
    private Map<String, UUID> transferToDevice = new HashMap<>();
    private Map<String, UUID> transferOnDevice = new HashMap<>();
    private List<String> deleteFromDevice = new ArrayList<>();

    private Map<String, UUID> transferToLibrary = new HashMap<>();
    private Map<String, UUID> transferOnLibrary = new HashMap<>();
    private List<String> deleteFromLibrary = new ArrayList<>();
}
