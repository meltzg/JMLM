package org.meltzg.jmlm.ui;

import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.wizard.DeviceNamePane;
import org.meltzg.jmlm.ui.components.controls.wizard.DeviceWizardPane;
import org.meltzg.jmlm.ui.components.controls.wizard.LibraryRootSelectionPane;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;

@Slf4j
public class DeviceWizard {

    @Autowired
    private FileSystemAudioContentDeviceRepository deviceRepository;

    @Autowired
    private AudioContentRepository contentRepository;

    private Wizard wizard;

    private FileSystemAudioContentDevice device;

    public DeviceWizard() {
        wizard = new Wizard();
    }

    public void show() {
        device = new FileSystemAudioContentDevice(contentRepository);
        var page1 = new DeviceWizardPane(new DeviceNamePane());
        var page2 = new DeviceWizardPane(new LibraryRootSelectionPane(device));
        var page3 = new WizardPane();

        wizard.setFlow(new Wizard.LinearFlow(page1, page2, page3));
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                log.info("Wizard finished, settings: " + wizard.getSettings());
                buildAndSaveDevice();
            }
        });
    }

    private void buildAndSaveDevice() {
        var settings = wizard.getSettings();
        device.setName((String) settings.get("deviceName"));
        var libraryRoots = (Collection<Path>) settings.get("libraryRoots");
        for (var root : libraryRoots) {
            try {
                device.addLibraryRoot(root.toString());
            } catch (IOException | URISyntaxException e) {
                log.error(String.format("Could not add %s to device", root), e);
            }
        }
        deviceRepository.save(device);
    }
}
