package org.meltzg.jmlm.ui.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

@AllArgsConstructor
public class DeviceWrapper {
    @Getter
    FileSystemAudioContentDevice device;

    @Override
    public String toString() {
        return device.getName();
    }
}
