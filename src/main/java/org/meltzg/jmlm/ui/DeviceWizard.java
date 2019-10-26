package org.meltzg.jmlm.ui;

import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.meltzg.jmlm.device.DeviceType;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.MTPAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.components.controls.wizard.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

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
        wizard.setFlow(new DeviceWizardFlow());
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
        try {


            device.mount();
            var libraryRoots = (Collection<String>) settings.get("libraryRoots");
            for (var root : libraryRoots) {
                try {
                    device.addLibraryRoot(root);
                } catch (IOException | URISyntaxException e) {
                    log.error(String.format("Could not add %s to device", root), e);
                }
            }
            deviceRepository.save(device);
            device.unmount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DeviceWizardFlow implements Wizard.Flow {
        WizardPane finalPage = new WizardPane();

        @Override
        public Optional<WizardPane> advance(WizardPane currentPage) {
            if (currentPage == null) {
                return Optional.of(new DeviceWizardPane(new DeviceTypePane()));
            }
            var currentDevicePage = (DeviceWizardPane) currentPage;
            if (currentDevicePage.getControl() instanceof DeviceTypePane) {
                log.debug("FOUND DEVICE TYPE PANE");
                var deviceTypePane = (DeviceTypePane) currentPage.getContent();
                if (deviceTypePane.getDeviceType() == DeviceType.MTP) {
                    log.info("configure MTP device");
                    device = new MTPAudioContentDevice(contentRepository);
                    return Optional.of(new DeviceWizardPane(new DeviceListPane(device)));
                } else if (deviceTypePane.getDeviceType() == DeviceType.FILESYSTEM) {
                    log.info("configure Filesystem device");
                    device = new FileSystemAudioContentDevice(contentRepository);
                    return Optional.of(new DeviceWizardPane(new DeviceNamePane()));
                }
            }
            if (currentDevicePage.getControl() instanceof DeviceListPane) {
                device = ((DeviceListPane) currentDevicePage.getControl()).getDevice();
                return Optional.of(new DeviceWizardPane(new DeviceNamePane()));
            }
            if (currentDevicePage.getControl() instanceof DeviceNamePane) {
                try {
                    return Optional.of(new DeviceWizardPane(new LibraryRootSelectionPane(device)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                device.unmount();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Optional.ofNullable(finalPage);
        }

        @Override
        public boolean canAdvance(WizardPane currentPage) {
            return currentPage != finalPage;
        }
    }
}
