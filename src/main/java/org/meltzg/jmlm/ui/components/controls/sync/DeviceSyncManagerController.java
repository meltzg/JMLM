package org.meltzg.jmlm.ui.components.controls.sync;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import lombok.Setter;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.types.DeviceWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DeviceSyncManagerController implements DialogController, Initializable {
    @Setter
    private FXMLDialog dialog;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;
    @FXML
    private ChoiceBox<DeviceWrapper> chcLibrary;
    @FXML
    private ChoiceBox<DeviceWrapper> chcAttached;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshDevices();
    }

    public void refreshDevices() {
        var devices = new ArrayList<DeviceWrapper>();
        for (var device : deviceRepository.findAll()) {
            devices.add(new DeviceWrapper(device));
        }
        chcLibrary.getItems().setAll(devices);
        chcAttached.getItems().setAll(devices);
    }
}
