package org.meltzg.jmlm.ui.components.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

import java.io.IOException;

public class DeviceNamePane extends DeviceWizardPane {

    public TextField deviceName;

    public DeviceNamePane() {
        var loader = new FXMLLoader(getClass().getResource("DeviceNamePaneView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void validate() {
        if (deviceName.getText().isEmpty()) {
            throw new IllegalStateException("Invalid device name");
        }
    }

    @Override
    public void configure(FileSystemAudioContentDevice device) {
        validate();
        device.setName(deviceName.getText());
    }
}
