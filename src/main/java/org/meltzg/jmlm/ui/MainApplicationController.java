package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import lombok.Setter;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;

public class MainApplicationController implements DialogController {
    private ScreensConfiguration screens;
    @Setter private FXMLDialog dialog;

    public MainApplicationController(ScreensConfiguration screens) {
        this.screens = screens;
    }

    public void exit(ActionEvent actionEvent) {
        dialog.close();
    }

    public void openDeviceManager(ActionEvent actionEvent) {
        screens.deviceManager().show();
    }
}
