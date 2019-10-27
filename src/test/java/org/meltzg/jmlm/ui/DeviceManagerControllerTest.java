package org.meltzg.jmlm.ui;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.JmlmApplication;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmlmApplication.class, ScreensConfiguration.class})
public class DeviceManagerControllerTest extends ApplicationTest {
    @Autowired
    ScreensConfiguration screens;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;

    private Scene scene;

    @After
    public void tearDown() {
        deviceRepository.deleteAll();
    }

    @Override
    public void start(Stage stage) throws Exception {
        scene = screens.deviceManager().getScene();
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testRefreshDeviceList() {
        var deviceList = (TableView) lookup("#tblDevices").query();
        assertEquals(0, deviceList.getItems().size());
        deviceRepository.save(new FileSystemAudioContentDevice("Test", null));
        clickOn("Refresh List");
        assertEquals(1, deviceList.getItems().size());
    }

    @Test
    public void testDeleteDevice() {
        var deviceList = (TableView) lookup("#tblDevices").query();
        assertEquals(0, deviceList.getItems().size());
        deviceRepository.save(new FileSystemAudioContentDevice("Test", null));
        clickOn("Refresh List");

        clickOn("Test");
        clickOn("Delete Device");
        assertEquals(0, deviceList.getItems().size());
        assertEquals(0, deviceRepository.count());
    }

    @Test
    public void testAddDevice() {
        clickOn("Add Device");
        clickOn("#radFsDevice");
        clickOn("Next");
        clickOn("#deviceName").write("Test");
        clickOn("Next");

        var pathToAdd = Paths.get(".").toAbsolutePath().toString().replace(".", "");
        pathToAdd += "/gradle";
        var deviceTree = (TreeView) lookup("#deviceTree").query();

        doubleClickOn("/");
        for (var part : pathToAdd.split("/")) {
            if (part.length() > 0) {
                while (!((TreeItem) deviceTree.getSelectionModel().getSelectedItem()).getValue().toString().equals(part)) {
                    var currentSelection = deviceTree.getSelectionModel().getSelectedItem();
                    type(KeyCode.DOWN);
                    if (currentSelection.equals(deviceTree.getSelectionModel().getSelectedItem())) {
                        fail("Could not select " + pathToAdd);
                    }
                }
                doubleClickOn(part);
            }
        }

        clickOn("Add to Device").clickOn("Next");
        type(KeyCode.ENTER);
        assertEquals(1, deviceRepository.count());
    }
}