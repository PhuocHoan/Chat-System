package com.haichutieu.chatsystem.client.util;

import com.haichutieu.chatsystem.client.ClientApp;
import com.haichutieu.chatsystem.client.SocketClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class SceneController {
    public static Stage primaryStage;
    public static HashMap<String, Scene> scenes = new HashMap<>();

    public static void initScenes() throws IOException {
        addScene("login", "gui/login.fxml", "../stylesheets/style.css");
        addScene("register", "gui/signup.fxml", "../stylesheets/style.css");
        addScene("chat", "gui/chat.fxml", "../stylesheets/style.css");
        addScene("adminLogin", "gui/adminPanel/adminLogin.fxml", "../stylesheets/adminPanel.css");
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setOnCloseRequest(e -> {
            if (SessionManager.getInstance().getCurrentUser() != null) {
                SocketClient.getInstance().sendMessages("OFFLINE " + SessionManager.getInstance().getCurrentUser().getUsername() + " " + SessionManager.getInstance().getCurrentUser().getId() + " " + SessionManager.numberPeopleChatWith + " " + SessionManager.numberGroupChatWith);
            } else {
                SocketClient.getInstance().sendMessages("OFFLINE null");
            }
        });
    }

    public static void setScene(String name) {
        primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        primaryStage.setScene(scenes.get(name));
        primaryStage.show();
    }

    public static void addScene(String name, String fxml, String css) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(ClientApp.class.getResource(css)).toExternalForm()); // apply style.css to javafx
        scenes.put(name, scene);
    }
}
