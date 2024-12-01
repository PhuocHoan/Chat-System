package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException, ExecutionException, InterruptedException {
        try {
            SceneController.setPrimaryStage(primaryStage);
            SceneController.initScenes();
            SceneController.primaryStage.setTitle("Chat System");
            SceneController.setScene("chat");
            SceneController.primaryStage.setMinHeight(770);
            SceneController.primaryStage.setMinWidth(1280);
            SceneController.primaryStage.setMaximized(true);
            SceneController.primaryStage.show();

            SocketClient client = SocketClient.getInstance();
            client.sendMessage("GET_FRIEND_LIST 1");
            Thread.sleep(5000);
            client.sendMessage("GET_FRIEND_LIST 2");
        } catch (IOException e) {
            e.printStackTrace(); // Handle connection error (e.g., show an alert)
            System.out.println("Failed connecting to server!");
        }
    }
}