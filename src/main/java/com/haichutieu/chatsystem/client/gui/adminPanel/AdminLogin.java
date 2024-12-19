package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.UnaryOperator;

public class AdminLogin {
    private static AdminLogin instance;

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    @FXML
    private HBox screen;

    public AdminLogin() {
        instance = this;
    }

    public static AdminLogin getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        // Handle the enter key event
        screen.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // When the screen is clicked, it gains focus, causing any TextField to lose focus
        screen.setOnMouseClicked(event -> screen.requestFocus());

        // limit text length in textField. Max length: 32
        UnaryOperator<TextFormatter.Change> limitLength = c -> {
            if (c.getControlNewText().length() > 32) {
                return null;
            }
            return c;
        };

        username.setTextFormatter(new TextFormatter<>(limitLength));
        password.setTextFormatter(new TextFormatter<>(limitLength));
    }

    @FXML
    void login() {
        handleLogin();
    }

    void handleLogin() {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            showAlert("Error", "Empty fields", "Please fill in all fields");
        } else {
            // Send the username and password to the server
            SocketClient.getInstance().sendMessages("LOGIN_ADMIN " + username.getText() + " " + password.getText());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onLoginResponse(boolean status, String message) {
        Platform.runLater(() -> {
            if (status) {
                // Open the admin panel
                try {
                    SceneController.addScene("adminPanel", "gui/adminPanel/adminPanel.fxml", "stylesheets/adminPanel.css");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                SceneController.setScene("adminPanel");
                return;
            }

            // Show an error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login failed");
            alert.setContentText(message);
            alert.show();
            return;
        });
    }
}
