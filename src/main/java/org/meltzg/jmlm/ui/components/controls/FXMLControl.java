package org.meltzg.jmlm.ui.components.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;

public abstract class FXMLControl extends Pane {
    public FXMLControl() {
        var loader = new FXMLLoader(getClass().getResource(getClass().getSimpleName() + "View.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
