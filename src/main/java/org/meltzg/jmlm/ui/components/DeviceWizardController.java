package org.meltzg.jmlm.ui.components;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.DeviceNameControl;
import org.meltzg.jmlm.ui.components.controls.ValidatableControl;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.ResourceBundle;


public class DeviceWizardController implements DialogController, Initializable {
    @FXML
    StackPane deviceBuilder;

    @FXML
    Button btnBack;

    @FXML
    Button btnNext;

    @FXML
    Button btnCreate;

    @Setter
    private FXMLDialog dialog;

    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;

    @Autowired
    private AudioContentRepository contentRepository;

    private ObservableList<ValidatableControl> configurationControls;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deviceBuilder.getChildren().setAll(new DeviceNameControl());
    }

    public void goBackControl(ActionEvent actionEvent) {
    }

    public void goNextControl(ActionEvent actionEvent) {
    }

    public void createDevice(ActionEvent actionEvent) {
        var newDevice = new FileSystemAudioContentDevice("Device", contentRepository);
        deviceRepository.save(newDevice);
        closeWizard(actionEvent);
    }

    public void closeWizard(ActionEvent actionEvent) {
        dialog.close();
    }
}
