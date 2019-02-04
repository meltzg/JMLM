package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceManagerController {
    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;

    @FXML
    private Pane deviceList;

    @FXML
    private Pane deviceWizard;

    public void openWizard(ActionEvent actionEvent) {
        deviceList.setVisible(false);
        deviceWizard.toFront();
        deviceWizard.setVisible(true);
    }

    public void closeWizard(ActionEvent actionEvent) {
        deviceWizard.setVisible(false);
        deviceList.toFront();
        deviceList.setVisible(true);
    }


}
