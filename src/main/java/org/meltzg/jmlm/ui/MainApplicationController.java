package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import lombok.Setter;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.components.controls.sync.DeviceSyncManagerController;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainApplicationController implements DialogController, Initializable {
    private ScreensConfiguration screens;
    @Setter private FXMLDialog dialog;

    @FXML
    private BorderPane mainView;

    private DeviceSyncManagerController syncManager;

    public MainApplicationController(ScreensConfiguration screens) {
        this.screens = screens;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        syncManager = screens.deviceSyncManagerController();
        var syncManagerView = screens.deviceSyncManager(syncManager).getScene().getRoot();
//        mainView.setCenter(syncManager);
        mainView.setCenter(syncManagerView);
    }

    public void exit(ActionEvent actionEvent) {
        dialog.close();
    }

    public void openDeviceManager(ActionEvent actionEvent) {
        screens.deviceManager().showAndWait();
        syncManager.refreshDevices();
    }
}
