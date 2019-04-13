package org.meltzg.jmlm.ui.components;

import javafx.scene.control.Alert;

public interface DialogController {
    void setDialog(FXMLDialog dialog);

    default void showAlert(String title, String header, String content, Alert.AlertType type) {
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
