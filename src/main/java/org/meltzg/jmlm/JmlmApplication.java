package org.meltzg.jmlm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class JmlmApplication extends Application {

    public static ConfigurableApplicationContext CONTEXT;
    private Parent root;

    public static void main(String[] args) {
        launch(JmlmApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        CONTEXT = SpringApplication.run(JmlmApplication.class);
        var fxmlLoader = new FXMLLoader(getClass().getResource("/org/meltzg/jmlm/ui/MainApplicationView.fxml"));
        fxmlLoader.setControllerFactory(CONTEXT::getBean);
        root = fxmlLoader.load();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Java Media Library Manager");
        var scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        CONTEXT.stop();
    }
}

