package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Setter;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.stereotype.Component;

@Component
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
