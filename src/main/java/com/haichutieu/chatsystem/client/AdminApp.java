package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            SceneController.setPrimaryStage(primaryStage);
            SceneController.initScenes();
            SceneController.primaryStage.setTitle("Chat System");
            SceneController.setScene("adminPanel");
            SceneController.primaryStage.setMinHeight(770);
            SceneController.primaryStage.setMinWidth(1280);
            SceneController.primaryStage.setMaximized(true);
            SceneController.primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle connection error (e.g., show an alert)
            System.out.println("Failed connecting to server!");
        }
    }
}
