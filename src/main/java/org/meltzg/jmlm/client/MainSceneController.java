package org.meltzg.jmlm.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainSceneController {

    @FXML
    private TextField txtDateTime;

    public void showDateTime(ActionEvent actionEvent) {
        var now = new Date();
        var dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        txtDateTime.setText(dateFormat.format(now));
    }
}
