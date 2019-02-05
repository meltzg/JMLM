package org.meltzg.jmlm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
@Slf4j
public class JmlmApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var context = new AnnotationConfigApplicationContext(JmlmApplicationConfiguration.class);
        var screens = context.getBean(ScreensConfiguration.class);
        screens.setPrimaryStage(primaryStage);
        screens.mainView().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

