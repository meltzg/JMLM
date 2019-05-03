package org.meltzg.jmlm.device;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ListableDevice extends MountableDevice {
    List<Map<String, String>> getAllDeviceMountProperties() throws IOException;
}
