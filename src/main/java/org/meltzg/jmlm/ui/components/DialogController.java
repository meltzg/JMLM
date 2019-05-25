package org.meltzg.jmlm.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public interface DialogController {
    void setDialog(FXMLDialog dialog);

    default Optional<ButtonType> showAlert(String title, String header, String content, Alert.AlertType type, ButtonType... buttons) {
        var alert = new Alert(type, content, buttons);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setResizable(true);
        return alert.showAndWait();
    }
}
