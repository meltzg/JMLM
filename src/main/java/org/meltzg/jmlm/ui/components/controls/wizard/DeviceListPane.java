package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.ListableDevice;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class DeviceListPane extends ValidatableControl {
    private final FileSystemAudioContentDevice device;

    ObservableList<Map<String, String>> mountProperties = FXCollections.observableArrayList();

    @FXML
    private ComboBox cmbDeviceSelection;

    public DeviceListPane(FileSystemAudioContentDevice device) {
        this.device = device;
        var listableDevice = (ListableDevice) device;
        try {
            cmbDeviceSelection.getItems().setAll(listableDevice.getAllDeviceMountProperties());
        } catch (IOException e) {
            log.error("Could not set device", e);
        }
    }

    @Override
    public void registerValidators(ValidationSupport vs) {
    }

    public FileSystemAudioContentDevice getDevice() {
        var selectedItem = cmbDeviceSelection.getSelectionModel().getSelectedItem();
        log.info("OBJ %s", selectedItem);
        device.setMountProperties((Map<String, String>) selectedItem);
        return device;
    }
}
