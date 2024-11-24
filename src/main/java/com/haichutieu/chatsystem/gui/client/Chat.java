package com.haichutieu.chatsystem.gui.client;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;

public class Chat {

    @FXML
    private GridPane screen;

    @FXML
    public void initialize() {
        screen.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        screen.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());
    }
}
