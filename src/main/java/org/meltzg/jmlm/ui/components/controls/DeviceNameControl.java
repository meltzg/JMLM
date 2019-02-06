package org.meltzg.jmlm.ui.components.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

import java.io.IOException;

public class DeviceNameControl extends VBox implements ValidatableControl {

    public TextField deviceName;

    @Setter
    FileSystemAudioContentDevice device;

    public DeviceNameControl() {
        var loader = new FXMLLoader(getClass().getResource("DeviceNameControlView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean validate() {
        return !deviceName.getText().isEmpty();
    }

    @Override
    public boolean configure() {
        if (!validate()) {
            return false;
        }
        device.setName(deviceName.getText());
        return true;
    }
}
