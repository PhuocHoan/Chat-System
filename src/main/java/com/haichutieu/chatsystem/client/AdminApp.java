package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneController.setPrimaryStage(primaryStage, "adminLogin");
    }
}
