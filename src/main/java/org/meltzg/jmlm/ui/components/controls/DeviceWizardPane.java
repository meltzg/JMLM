package org.meltzg.jmlm.ui.components.controls;

import javafx.scene.layout.Pane;
import lombok.Setter;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

public abstract class DeviceWizardPane<T extends FileSystemAudioContentDevice> extends Pane implements ValidatableControl<T> {
    @Setter
    T device;

    public abstract void configure(T device);
}
