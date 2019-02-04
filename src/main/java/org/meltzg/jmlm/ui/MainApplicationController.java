package org.meltzg.jmlm.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainApplicationController {
    @FXML
    private Pane mainView;

    public void exit(ActionEvent actionEvent) {
        var stage = (Stage) mainView.getScene().getWindow();
        stage.close();
    }
}
