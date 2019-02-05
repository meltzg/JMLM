package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.stereotype.Component;

@Component
public class MainApplicationController implements DialogController {
    private ScreensConfiguration screens;
    private FXMLDialog dialog;

    public MainApplicationController(ScreensConfiguration screens) {
        this.screens = screens;
    }

    @Override
    public void setDialog(FXMLDialog dialog) {
        this.dialog = dialog;
    }

    public void exit(ActionEvent actionEvent) {
        dialog.close();
    }

    public void openDeviceManager(ActionEvent actionEvent) {
        screens.deviceManager().show();
    }
}
