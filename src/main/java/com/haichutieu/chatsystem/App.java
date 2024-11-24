package com.haichutieu.chatsystem;

import com.haichutieu.chatsystem.bus.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneController.setPrimaryStage(primaryStage);
        SceneController.initScenes();
        SceneController.primaryStage.setTitle("Chat System");
        SceneController.setScene("login");
        SceneController.primaryStage.setMinHeight(770);
        SceneController.primaryStage.setMinWidth(1280);
        SceneController.primaryStage.setMaximized(true);
        SceneController.primaryStage.show();
    }
}