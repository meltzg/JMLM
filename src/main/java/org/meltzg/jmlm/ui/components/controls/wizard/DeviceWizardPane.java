package org.meltzg.jmlm.ui.components.controls.wizard;

import lombok.Getter;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.ValidationSupport;

public class DeviceWizardPane<T extends ValidatableControl> extends WizardPane {
    @Getter
    private final T control;
    private ValidationSupport vs = new ValidationSupport();

    public DeviceWizardPane(T control) {
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
