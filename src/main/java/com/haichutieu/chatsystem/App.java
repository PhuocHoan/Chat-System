package com.haichutieu.chatsystem;

import com.haichutieu.chatsystem.client.Login;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
@Override
public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm()); // apply style.css to javafx
    stage.setTitle("Chat System");
    stage.setScene(scene);
    stage.setMinHeight(720);
    stage.setMinWidth(1280);
    stage.setMaximized(true);
    stage.show();
}