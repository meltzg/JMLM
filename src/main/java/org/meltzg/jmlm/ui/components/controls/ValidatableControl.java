package org.meltzg.jmlm.ui.components.controls;

import org.controlsfx.validation.ValidationSupport;

import java.util.HashMap;
import java.util.Map;

public abstract class ValidatableControl extends FXMLControl {
    public abstract void registerValidators(ValidationSupport vs);

    public Map<String, Object> getAdditionalSettings() {
        return new HashMap<>();
    }
}
