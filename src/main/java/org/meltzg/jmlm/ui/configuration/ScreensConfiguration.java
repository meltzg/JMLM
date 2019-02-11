package org.meltzg.jmlm.ui.configuration;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import org.meltzg.jmlm.ui.DeviceManagerController;
import org.meltzg.jmlm.ui.DeviceWizard;
import org.meltzg.jmlm.ui.MainApplicationController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.components.controls.sync.DeviceSyncManagerController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
@Lazy
public class ScreensConfiguration {
    @Setter
    private Stage primaryStage;

    public void showScreen(Parent screen) {
        primaryStage.setScene(new Scene(screen));
        primaryStage.show();
    }

    @Bean
    public FXMLDialog mainView() {
        return new FXMLDialog(mainViewController(), getClass().getResource("/org/meltzg/jmlm/ui/MainApplicationView.fxml"), primaryStage);
    }

    @Bean
    MainApplicationController mainViewController() {
        return new MainApplicationController(this);
    }

    @Bean
    public FXMLDialog deviceManager() {
        return new FXMLDialog(deviceManagerController(), getClass().getResource("/org/meltzg/jmlm/ui/DeviceManagerView.fxml"), primaryStage);
    }

    @Bean
    DeviceManagerController deviceManagerController() {
        return new DeviceManagerController(this);
    }

    @Bean
    @Scope("prototype")
    public DeviceWizard deviceWizard() {
        return new DeviceWizard();
    }

    @Bean
    public FXMLDialog deviceSyncManager() {
        return new FXMLDialog(deviceSyncManagerController(), getClass().getResource("/org/meltzg/jmlm/ui/components/controls/sync/DeviceSyncManagerControllerView.fxml"),
                primaryStage, StageStyle.DECORATED, Modality.APPLICATION_MODAL);
    }

    @Bean
    public FXMLDialog deviceSyncManager(DeviceSyncManagerController controller) {
        return new FXMLDialog(controller, getClass().getResource("/org/meltzg/jmlm/ui/components/controls/sync/DeviceSyncManagerControllerView.fxml"),
                primaryStage, StageStyle.DECORATED, Modality.APPLICATION_MODAL);
    }

    @Bean
    public DeviceSyncManagerController deviceSyncManagerController() {
        return new DeviceSyncManagerController();
    }
}
