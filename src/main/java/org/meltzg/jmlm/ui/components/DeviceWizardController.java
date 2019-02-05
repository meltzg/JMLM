package org.meltzg.jmlm.ui.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DeviceWizardController implements DialogController {
    @FXML
    StackPane deviceBuilder;

    private FXMLDialog dialog;

    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;


    @Override
    public void setDialog(FXMLDialog dialog) {
        this.dialog = dialog;
    }

    public void createDevice(ActionEvent actionEvent) {
        closeWizard(actionEvent);
    }

    public void closeWizard(ActionEvent actionEvent) {
        dialog.close();
    }
}
