package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class DeviceNamePane extends ValidatableControl {

    @FXML
    private TextField deviceName;

    @Override
    public void registerValidators(ValidationSupport vs) {
        vs.registerValidator(deviceName, Validator.createEmptyValidator("EMPTY!"));
    }
}
