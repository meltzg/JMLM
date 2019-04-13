package org.meltzg.jmlm.ui.components.controls.sync;

import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.JmlmApplication;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.sync.DeviceSyncManager;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.meltzg.jmlm.CommonUtil.RESOURCEDIR;
import static org.meltzg.jmlm.CommonUtil.TMPDIR;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmlmApplication.class, ScreensConfiguration.class})
public class DeviceSyncManagerControllerTest extends ApplicationTest {
    @Autowired
    ScreensConfiguration screens;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;

    @Autowired
    AudioContentRepository contentRepository;

    private Scene scene;

    private DeviceSyncManager syncManager;
    private FileSystemAudioContentDevice device1;
    private FileSystemAudioContentDevice device2;
    private FileSystemAudioContentDevice device3;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        device1 = new FileSystemAudioContentDevice("device1", contentRepository);
        device2 = new FileSystemAudioContentDevice("device2", contentRepository);
        device3 = new FileSystemAudioContentDevice("device3", contentRepository);

        device1.addLibraryRoot(Paths.get(RESOURCEDIR, "audio/jst2018-12-09").toString());
        device2.addLibraryRoot(Paths.get(RESOURCEDIR, "audio/kwgg2016-10-29").toString());

        try {
            FileUtils.cleanDirectory(Paths.get(TMPDIR).toFile());
        } catch (IllegalArgumentException e) {
            Paths.get(TMPDIR).toFile().mkdirs();
        }

        device3.addLibraryRoot(TMPDIR);
        deviceRepository.saveAll(Arrays.asList(device1, device2, device3));
        clickOn("Refresh Devices");
    }

    @After
    public void tearDown() {
        deviceRepository.deleteAll();
    }

    @Override
    public void start(Stage stage) throws Exception {
        scene = screens.deviceSyncManager(screens.deviceSyncManagerController()).getScene();
        stage.setScene(scene);
        stage.show();
    }

    @Test

    public void testLoadDeviceLists() {
        ChoiceBox chcLibrary = lookup("#chcLibrary").query();
        assertEquals(deviceRepository.count(), chcLibrary.getItems().size());

        ChoiceBox chcAttached = lookup("#chcAttached").query();
        assertEquals(deviceRepository.count(), chcAttached.getItems().size());
    }

    @Test
    public void testLoadMergedContent() {
        clickOn("#chcLibrary");
        clickOn("device1");
        clickOn("#chcAttached");
        clickOn("device2");

        TableView<DeviceSyncManagerController.SelectedContent> contentTable = lookup("#contentTable")
                .queryTableView();
        var content = contentTable.getItems();

        assertEquals(contentRepository.count(), content.size());
        for (var item : content) {
            var itemInfo = item.getContentSyncStatusProperty().get();
            var checked = item.getSelectedProperty().get();
            assertEquals(checked, itemInfo.isOnDevice());
        }
    }

    @Test
    public void resetSelection() {
        var robot = new FxRobot();
        clickOn("#chcLibrary");
        clickOn("device1");
        clickOn("#chcAttached");
        clickOn("device2");

        TableView<DeviceSyncManagerController.SelectedContent> contentTable = lookup("#contentTable")
                .queryTableView();
        var content = contentTable.getItems();
        for (var contentItem : content) {
            robot.interact(() -> {
                contentItem.getSelectedProperty().set(!contentItem.getSelectedProperty().get());
            });
            assertNotEquals(contentItem.getContentSyncStatusProperty().get().isOnDevice(),
                    contentItem.getSelectedProperty().get());
        }

        clickOn("#btnResetSelection");

        for (var contentItem : content) {
            assertEquals(contentItem.getContentSyncStatusProperty().get().isOnDevice(),
                    contentItem.getSelectedProperty().get());
        }
    }
}