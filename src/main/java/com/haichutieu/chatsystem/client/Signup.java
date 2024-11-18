package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Signup {

    @FXML
    private Label alreadyAccount;

    @FXML
    private Button button;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    void switchToLogin(MouseEvent event) {
        try {
            Stage stage = (Stage) alreadyAccount.getScene().getWindow();
            Parent fxmlLoader = FXMLLoader.load(App.class.getResource("client/login.fxml"));
            Scene scene = new Scene(fxmlLoader);
            scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
