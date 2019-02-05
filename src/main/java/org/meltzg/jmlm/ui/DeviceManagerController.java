package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Component
public class DeviceManagerController implements DialogController, Initializable {
    private ScreensConfiguration screens;

    private FXMLDialog dialog;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;

    @FXML
    TableView<FileSystemAudioContentDevice> tblDevices;
    @FXML
    TableColumn<FileSystemAudioContentDevice, String> colDeviceType;
    @FXML
    TableColumn<FileSystemAudioContentDevice, String> colDeviceName;

    @Override
    public void setDialog(FXMLDialog dialog) {
        this.dialog = dialog;
    }

    public DeviceManagerController(ScreensConfiguration screens) {
        this.screens = screens;
    }

    public void openWizard(ActionEvent actionEvent) {
        screens.deviceWizard().show();
    }

    public void close(ActionEvent actionEvent) {
        dialog.close();
    }

    public void refreshDevices() {
        var devices = new ArrayList<FileSystemAudioContentDevice>();
        deviceRepository.findAll().forEach(devices::add);
        tblDevices.getItems().setAll(devices);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDeviceName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDeviceType.setCellValueFactory(new PropertyValueFactory<>("type"));

        refreshDevices();
    }
}
