package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.device.DeviceType;

public class DeviceTypePane extends ValidatableControl {
    @FXML
    ToggleGroup toggleGroup;
    @FXML
    RadioButton radFsDevice;
    @FXML
    RadioButton radMtpDevice;

    @Override
    public void registerValidators(ValidationSupport vs) {

    }

    public DeviceType getDeviceType() {
        if (radMtpDevice.selectedProperty().get()) {
            return DeviceType.MTP;
        }
        return DeviceType.FILESYSTEM;
    }
}
