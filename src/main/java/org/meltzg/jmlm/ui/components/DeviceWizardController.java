package org.meltzg.jmlm.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.DeviceNamePane;
import org.meltzg.jmlm.ui.components.controls.DeviceWizardPane;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


@Slf4j
public class DeviceWizardController implements DialogController, Initializable {
    @FXML
    BorderPane deviceBuilder;

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

    private List<DeviceWizardPane> wizardPanes;

    private IntegerProperty wizardIndex = new SimpleIntegerProperty();

    private FileSystemAudioContentDevice device;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        device = new FileSystemAudioContentDevice(contentRepository);
        wizardPanes = Arrays.asList(new DeviceNamePane(), new DeviceNamePane());

        wizardIndex.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() <= 0) {
                btnBack.disableProperty().set(true);
            } else {
                btnBack.disableProperty().set(false);
            }
            if (newValue.intValue() >= wizardPanes.size() - 1) {
                btnNext.disableProperty().set(true);
            } else {
                btnNext.disableProperty().set(false);
            }
        });

        wizardIndex.set(-1);
        goNextControl(null);
    }

    public void goBackControl(ActionEvent actionEvent) {
        if (wizardIndex.get() > 0) {
            wizardIndex.set(wizardIndex.get() - 1);
            var nextPane = wizardPanes.get(wizardIndex.get());
            nextPane.setDevice(device);
            deviceBuilder.setCenter(nextPane);
        }
    }

    public void goNextControl(ActionEvent actionEvent) {
        try {
            if (wizardIndex.get() >= 0) {
                wizardPanes.get(wizardIndex.get()).validate();
            }
        } catch (Exception e) {
            handleStateException(e);
        }
        if (wizardIndex.get() < wizardPanes.size() - 1) {
            wizardIndex.set(wizardIndex.get() + 1);
            var nextPane = wizardPanes.get(wizardIndex.get());
            nextPane.setDevice(device);
            deviceBuilder.setCenter(nextPane);
        }
    }

    public void createDevice(ActionEvent actionEvent) {
        var newDevice = new FileSystemAudioContentDevice(contentRepository);
        try {
            for (var wizardPane : wizardPanes) {
                wizardPane.configure(newDevice);
            }
            deviceRepository.save(newDevice);
            closeWizard(actionEvent);
        } catch (IllegalStateException e) {
            handleStateException(e);
        }
    }

    public void closeWizard(ActionEvent actionEvent) {
        dialog.close();
    }

    private void handleStateException(Exception e) {
        var alert = new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage());
        alert.showAndWait();
        log.error(e.getMessage());
    }
}
