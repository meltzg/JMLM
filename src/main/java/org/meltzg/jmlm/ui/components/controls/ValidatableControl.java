package org.meltzg.jmlm.ui.components.controls;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

public interface ValidatableControl<T extends FileSystemAudioContentDevice> {
    boolean validate();
    boolean configure();
    void setDevice(T device);
}
