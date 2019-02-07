package org.meltzg.jmlm;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class JmlmApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var context = SpringApplication.run(JmlmApplication.class);
        var screens = context.getBean(ScreensConfiguration.class);
        screens.setPrimaryStage(primaryStage);
        screens.deviceManager().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

