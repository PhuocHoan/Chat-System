package com.haichutieu.chatsystem.client.gui.adminPanel;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AdminLogin {
    private static AdminLogin instance;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button submitButton;
    public AdminLogin() {
        instance = this;
    }

    public static AdminLogin getInstance() {
        return instance;
    }

    public void initialize() {
        submitButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Empty fields");
                alert.setContentText("Please fill in all fields");
                alert.showAndWait();
            } else {
                // Send the username and password to the server
                SocketClient.getInstance().sendMessages("LOGIN_ADMIN " + username + " " + password);
            }
        });
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onLoginResponse(String response) {
        Platform.runLater(() -> {
            switch (response.split(" ")[0]) {
                case "OK":
                    // Open the admin panel
                    try {
                        SceneController.addScene("adminPanel", "gui/adminPanel/adminPanel.fxml", "../stylesheets/adminPanel.css");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    SceneController.primaryStage.setMinHeight(770);
                    SceneController.primaryStage.setMinWidth(1280);
                    SceneController.primaryStage.setMaximized(true);
                    SceneController.primaryStage.setResizable(true);
                    SceneController.setScene("adminPanel");
                    break;
                case "INCORRECT":
                    showAlert("Error", "Incorrect username or password", "Please try again");
                    break;
                case "PROHIBITED":
                    showAlert("Error", "Access prohibited", "You do not have permission to access the admin panel");
                    break;
            }
        });
    }

    public void onGetAccountsResponse(String response) {
        Platform.runLater(() -> {

        });
    }
}
