package org.meltzg.jmlm.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.JmlmApplication;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmlmApplication.class, ScreensConfiguration.class})
public class MainApplicationControllerTest extends ApplicationTest {

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
        scene = screens.mainView().getScene();
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testExit() {
        clickOn("File");
        clickOn("Close");
    }

    @Test
    public void testOpenDeviceManager() throws TimeoutException {
        clickOn("File");
        clickOn("Device Manager");
        clickOn("Close");
        WaitForAsyncUtils.waitFor(100, TimeUnit.MILLISECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return lookup("Close").tryQuery().isEmpty();
            }
        });
    }
}