package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ForgotPasswordGUI {

    private static ForgotPasswordGUI instance;
    @FXML
    private HBox alert;

    @FXML
    private Text alertText;

    @FXML
    private TextField email;

    @FXML
    private VBox fieldContainer;

    @FXML
    private HBox screen;

    public ForgotPasswordGUI() {
        instance = this;
    }

    public static ForgotPasswordGUI getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        alert.setVisible(false);
        // Handle the enter key event
        screen.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleForgotPassword();
            }
        });

        // When the screen is clicked, it gains focus, causing any TextField to lose focus
        screen.setOnMouseClicked(event -> screen.requestFocus());

        // display error UI
        Text error = new Text();
        error.setStyle("-fx-fill: red");
        fieldContainer.getChildren().add(2, error);
        error.setVisible(false);

        email.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Field lost focus
                if (email.getText().isEmpty()) {
                    email.getStyleClass().add("error");
                    error.setText("Enter your email");
                    error.setVisible(true);
                } else if (!SignupGUI.getInstance().checkValidEmail(email.getText())) {
                    email.getStyleClass().add("error");
                    error.setText("Enter a valid email address");
                    error.setVisible(true);
                }
            } else {
                // Field gained focus
                email.getStyleClass().remove("error");
                error.setVisible(false);
            }
        });
    }

    @FXML
    void switchToLogin() {
        SceneController.setScene("login");
    }

    @FXML
    void switchToRegister() {
        SceneController.setScene("register");
    }

    @FXML
    void forgotPassword() {
        handleForgotPassword();
    }

    void handleForgotPassword() {
        if (email.getStyleClass().contains("error")) {
            AuthController.forgotPassword(email.getText(), "error");
        } else {
            AuthController.forgotPassword(email.getText(), null);
        }
    }

    public void displayError(String message) {
        alertText.setText(message);
        alert.setVisible(true);
    }

    public void forgotPasswordResult(String message) {
        Platform.runLater(() -> {
            if (!message.startsWith("ERROR")) {
                SceneController.setScene("login");
                System.out.println(message);
            } else {
                displayError(message.replaceFirst("ERROR", ""));
            }
        });
    }

}
