package com.haichutieu.chatsystem.client.util;

import com.haichutieu.chatsystem.client.ClientApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class SceneController {
    public static Stage primaryStage;
    public static HashMap<String, Scene> scenes = new HashMap<>();

    public static void initScenes() throws IOException {
        addScene("login", "gui/client/login.fxml", "stylesheets/style.css");
        addScene("register", "gui/client/signup.fxml", "stylesheets/style.css");
        addScene("chat", "gui/client/chat.fxml", "stylesheets/style.css");
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setScene(String name) {
        primaryStage.setScene(scenes.get(name));
    }

    public static void addScene(String name, String fxml, String css) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(ClientApp.class.getResource(css).toExternalForm()); // apply style.css to javafx
        scenes.put(name, scene);
    }
}
