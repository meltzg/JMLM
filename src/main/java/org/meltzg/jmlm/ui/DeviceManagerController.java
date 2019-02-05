package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceManagerController implements DialogController {
    private ScreensConfiguration screens;

    private FXMLDialog dialog;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;

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
}
