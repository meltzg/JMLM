package org.meltzg.jmlm.ui.components;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.meltzg.jmlm.JmlmApplication;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class DeviceWizardController extends BorderPane {
    private static final EventType<WizardCompleteEvent> WIZARD_COMPLETE = new EventType<>(Event.ANY, "WIZARD_COMPLETE");
    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;

    public void createDevice(ActionEvent actionEvent) {
        fireEvent(new WizardCompleteEvent());
    }

    public void closeWizard(ActionEvent actionEvent) {
        fireEvent(new WizardCompleteEvent());
    }

//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        var fxmlLoader = new FXMLLoader(getClass().getResource("/org/meltzg/jmlm/ui/MainApplicationView.fxml"));
//        fxmlLoader.setControllerFactory(JmlmApplication.CONTEXT::getBean);
//    }

    public static class WizardCompleteEvent extends Event {
        public WizardCompleteEvent() {
            super(WIZARD_COMPLETE);
        }
    }
}
