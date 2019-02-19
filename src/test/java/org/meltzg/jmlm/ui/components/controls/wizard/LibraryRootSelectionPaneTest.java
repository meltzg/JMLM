package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.testfx.framework.junit.ApplicationTest;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.meltzg.jmlm.CommonUtil.RESOURCEDIR;

public class LibraryRootSelectionPaneTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {
        var device = new FileSystemAudioContentDevice(null);
        device.setRootPath(Paths.get(RESOURCEDIR).toAbsolutePath().toString());
        var sceneRoot = new DeviceWizardPane(new LibraryRootSelectionPane(device));
        var scene = new Scene(sceneRoot);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testAddSelectedItem() {
        doubleClickOn("resources", MouseButton.PRIMARY);
        doubleClickOn("audio");
        clickOn("Add to Device");

        var libraryRoots = (ListView) lookup("#lstLibraryRoots").query();
        assertEquals(1, libraryRoots.getItems().size());
    }

    @Test
    public void testRemoveSelectedItem() {
        doubleClickOn("resources", MouseButton.PRIMARY);
        doubleClickOn("audio");
        clickOn("Add to Device");

        var libraryRoots = (ListView) lookup("#lstLibraryRoots").query();
        var selectedItem = libraryRoots.getItems().get(0).toString();
        clickOn(selectedItem);
        clickOn("Remove from Device");
        assertEquals(0, libraryRoots.getItems().size());
    }

    @Test
    public void testCannotAddChildren() {
        doubleClickOn("resources", MouseButton.PRIMARY);
        doubleClickOn("audio");
        clickOn("Add to Device");
        clickOn("jst2018-12-09");
        clickOn("Add to Device");
        type(KeyCode.ENTER);

        var libraryRoots = (ListView) lookup("#lstLibraryRoots").query();
        assertEquals(1, libraryRoots.getItems().size());
    }
}