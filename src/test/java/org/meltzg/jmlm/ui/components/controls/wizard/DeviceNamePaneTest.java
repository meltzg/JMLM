package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DeviceNamePaneTest extends ApplicationTest {
    private DeviceWizardPane sceneRoot;

    @Override
    public void start(Stage stage) throws Exception {
        sceneRoot = new DeviceWizardPane(new DeviceNamePane());
        var scene = new Scene(sceneRoot);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testValidateDeviceName() {
        assertTrue(sceneRoot.getVs().isInvalid());
        clickOn("#deviceName", MouseButton.PRIMARY).type(KeyCode.getKeyCode("A"));
        assertFalse(sceneRoot.getVs().isInvalid());
    }
}