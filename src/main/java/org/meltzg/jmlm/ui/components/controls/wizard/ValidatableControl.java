package org.meltzg.jmlm.ui.components.controls.wizard;

import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.ui.components.controls.FXMLControl;

import java.util.HashMap;
import java.util.Map;

public abstract class ValidatableControl extends FXMLControl {
    public abstract void registerValidators(ValidationSupport vs);

    public Map<String, Object> getAdditionalSettings() {
        return new HashMap<>();
    }
}
