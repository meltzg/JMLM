package org.meltzg.jmlm.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {

    @FXML
    private StackPane mainArea;

    @FXML
    private Pane libraryView;

    @FXML
    private Pane deviceManagerView;

    private static final String defaultView = "LibraryManagerView.fxml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void openDeviceManager(ActionEvent actionEvent) {
        deviceManagerView.toFront();
    }
}
