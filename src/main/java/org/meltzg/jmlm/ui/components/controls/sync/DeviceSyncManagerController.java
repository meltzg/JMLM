package org.meltzg.jmlm.ui.components.controls.sync;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.FXMLControl;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DeviceSyncManagerController extends FXMLControl implements Initializable {
    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;
    @FXML
    private ChoiceBox<FileSystemAudioContentDevice> chcLibrary;
    @FXML
    private ChoiceBox<FileSystemAudioContentDevice> chcAttached;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshDevices();
    }

    public void refreshDevices() {
        var devices = new ArrayList<FileSystemAudioContentDevice>();
        deviceRepository.findAll().forEach(devices::add);
        chcLibrary.getItems().setAll(devices);
        chcAttached.getItems().setAll(devices);
    }
}
