package org.meltzg.jmlm.ui.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class DeviceWizardController implements DialogController {
    @FXML
    StackPane deviceBuilder;

    @FXML
    TextField deviceName;

    @Setter
    private FXMLDialog dialog;

    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;

    @Autowired
    private AudioContentRepository contentRepository;

    public void createDevice(ActionEvent actionEvent) {
        var newDevice = new FileSystemAudioContentDevice(deviceName.getText(), contentRepository);
        deviceRepository.save(newDevice);
        closeWizard(actionEvent);
    }

    public void closeWizard(ActionEvent actionEvent) {
        dialog.close();
    }
}
