package org.meltzg.jmlm.ui.components.controls;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

public interface ValidatableControl<T extends FileSystemAudioContentDevice> {
    void validate() throws IllegalStateException;
}
