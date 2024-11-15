package com.haichutieu.chatsystem;

import com.haichutieu.chatsystem.client.Login;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("client/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm()); // apply style.css to javafx
            stage.setTitle("Chat System");
            stage.setScene(scene);
            stage.setMinHeight(700);
            stage.setMinWidth(900);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}