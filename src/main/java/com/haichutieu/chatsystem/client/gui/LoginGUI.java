package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.scene.input.MouseEvent;

public class LoginGUI {
    @FXML
    private Button button;

    @FXML
    private Label forgetPassword;

    @FXML
    private PasswordField password;

    @FXML
    private Label registerAccount;

    @FXML
    private TextField username;

    @FXML
    private HBox screen;

    @FXML
    public void initialize() {
        screen.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        screen.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());
    }

    @FXML
    void switchToRegister(MouseEvent event) {
        SceneController.setScene("register");
    }

    @FXML
    void login(MouseEvent event) {

    }
}
