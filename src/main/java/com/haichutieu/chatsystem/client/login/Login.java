package com.haichutieu.chatsystem.client.login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login {

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
    public void initialize() {
        System.out.println("hello");
    }
}
