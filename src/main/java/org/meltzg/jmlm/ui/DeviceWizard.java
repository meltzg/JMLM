package org.meltzg.jmlm.ui;

import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.DeviceNamePane;
import org.meltzg.jmlm.ui.components.controls.ValidatableControl;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DeviceWizard {

    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;

    @Autowired
    private AudioContentRepository contentRepository;

    private Wizard wizard;

    public DeviceWizard() {
        wizard = new Wizard();
    }

    public void show() {
        var page1 = makeWizardPane(new DeviceNamePane());
        var page2 = new WizardPane();
        wizard.setFlow(new Wizard.LinearFlow(page1, page2));
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                log.info("Wizard finished, settings: " + wizard.getSettings());
                buildAndSaveDevice();
            }
        });
    }

    private WizardPane makeWizardPane(ValidatableControl control) {
        return new WizardPane() {
            ValidationSupport vs = new ValidationSupport();

            {
                vs.initInitialDecoration();
                control.registerValidators(vs);
                setContent(control);
            }

            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                wizard.invalidProperty().bind(vs.invalidProperty());
            }
        };
    }

    private void buildAndSaveDevice() {
        var device = new FileSystemAudioContentDevice((String) wizard.getSettings().get("deviceName"), contentRepository);
        deviceRepository.save(device);
    }
}
