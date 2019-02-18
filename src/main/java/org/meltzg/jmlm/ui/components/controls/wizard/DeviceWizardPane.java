package org.meltzg.jmlm.ui.components.controls.wizard;

import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.ValidationSupport;

public class DeviceWizardPane extends WizardPane {
    private final ValidatableControl control;
    private ValidationSupport vs = new ValidationSupport();

    public DeviceWizardPane(ValidatableControl control) {
        this.control = control;
        vs.initInitialDecoration();
        this.control.registerValidators(vs);
        setContent(this.control);
    }

    @Override
    public void onEnteringPage(Wizard wizard) {
        wizard.invalidProperty().unbind();
        wizard.invalidProperty().bind(vs.invalidProperty());
    }

    @Override
    public void onExitingPage(Wizard wizard) {
        wizard.getSettings().putAll(control.getAdditionalSettings());
    }

    public ValidationSupport getVs() {
        return vs;
    }
}
