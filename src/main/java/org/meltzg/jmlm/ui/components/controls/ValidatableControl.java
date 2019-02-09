package org.meltzg.jmlm.ui.components.controls;

import org.controlsfx.validation.ValidationSupport;

public abstract class ValidatableControl extends FXMLControl {
    public abstract void registerValidators(ValidationSupport vs);
}
