package org.meltzg.jmlm.device;

import java.util.List;

public interface ListableDevice {
    List<FileSystemAudioContentDevice> listDevices();
}
