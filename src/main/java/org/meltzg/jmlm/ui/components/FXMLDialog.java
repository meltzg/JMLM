package org.meltzg.jmlm.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class FXMLDialog extends Stage {
    public FXMLDialog(DialogController controller, URL fxml, Window owner) {
        this(controller, fxml, owner, StageStyle.DECORATED);
    }

    public FXMLDialog(final DialogController controller, URL fxml, Window owner, StageStyle style) {
        super(style);
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        FXMLLoader loader = new FXMLLoader(fxml);
        try {
            loader.setControllerFactory(aClass -> controller);
            controller.setDialog(this);
            setScene(new Scene((Parent) loader.load()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
