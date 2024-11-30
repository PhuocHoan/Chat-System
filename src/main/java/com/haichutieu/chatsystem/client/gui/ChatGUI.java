package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class ChatGUI {

    @FXML
    private GridPane screen;

    @FXML
    public void initialize() {
        screen.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        screen.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());
    }

    @FXML
    public void switchToProfileTab() {

    }

    @FXML
    public void switchToFriendsTab(MouseEvent event) throws IOException {
        SceneController.addScene("friends", "gui/client/friends.fxml", "stylesheets/style.css");
        SceneController.setScene("friends");
    }
}
