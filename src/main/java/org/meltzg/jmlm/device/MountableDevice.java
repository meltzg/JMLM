package org.meltzg.jmlm.device;

import java.io.Closeable;
import java.util.Map;

public interface MountableDevice extends Closeable {
    Map<String, String> getMountProperties();
    void setMountProperties(Map<String, String> mountProperties);
}
