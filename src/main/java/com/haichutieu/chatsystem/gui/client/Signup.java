package com.haichutieu.chatsystem.gui.client;

import com.haichutieu.chatsystem.bus.AuthController;
import com.haichutieu.chatsystem.bus.SceneController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.UnaryOperator;

public class Signup {

    @FXML
    private Label alreadyAccount;

    @FXML
    private VBox fieldContainer;

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
    private TextField name;

    @FXML
    private HBox screen;

    @FXML
    private HBox alert;

    @FXML
    private Text alertText;

    @FXML
    public void initialize() {
        // Set the screen size to the size of the monitor
        screen.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        screen.setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight());

        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        alert.setVisible(false);
        // Handle the enter key event
        EventHandler<KeyEvent> eventHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRegister();
            }
        };
        screen.addEventHandler(KeyEvent.KEY_RELEASED, eventHandler);
        button.addEventHandler(KeyEvent.KEY_RELEASED, eventHandler);

        // When the screen is clicked, it gains focus, causing any TextField to lose focus
        screen.setOnMouseClicked(event -> {
            screen.requestFocus();
        });

        // limit text length in textField. Max length: 32
        UnaryOperator<TextFormatter.Change> limitLength = c -> {
            if (c.getControlNewText().length() > 32) {
                return null;
            }
            return c;
        };

        username.setTextFormatter(new TextFormatter<>(limitLength));
        password.setTextFormatter(new TextFormatter<>(limitLength));
        confirmPassword.setTextFormatter(new TextFormatter<>(limitLength));

        // Display error fields for each input
        displayErrorField(name, "name", 2);
        displayErrorField(username, "username", 5);
        displayErrorField(email, "email", 8);
        displayErrorField(password, "password", 11);
        displayErrorField(confirmPassword, "confirm password", 14);
    }

    <T extends TextField> void displayErrorField(T field, String element, int index) {
        Text error = new Text("Enter your " + element);
        error.setStyle("-fx-fill: red");
        fieldContainer.getChildren().add(index, error);
        error.setVisible(false);

        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Field lost focus
                if (field.getText().isEmpty()) {
                    field.getStyleClass().add("error");
                    error.setText("Enter your " + element);
                    error.setVisible(true);
                } else if (element.equals("username")) {
                    if (!checkValidUsername(field.getText())) {
                        field.getStyleClass().add("error");
                        error.setText("Username must be between 8 and 32 characters (A-Z, a-z, 0-9)");
                        error.setVisible(true);
                    }
                } else if (element.equals("email")) {
                    if (!checkValidEmail(field.getText())) {
                        field.getStyleClass().add("error");
                        error.setText("Enter a valid email address");
                        error.setVisible(true);
                    }
                } else if (element.equals("password")) {
                    if (!checkValidPassword(field.getText())) {
                        field.getStyleClass().add("error");
                        error.setText("Password must be between 8 and 32 characters (A-Z, a-z, 0-9)");
                        error.setVisible(true);
                    }
                } else if (element.equals("confirm password")) {
                    if (!field.getText().equals(password.getText())) {
                        field.getStyleClass().add("error");
                        error.setText("Passwords do not match");
                        error.setVisible(true);
                    }
                }
            } else {
                // Field gained focus
                field.getStyleClass().remove("error");
                error.setVisible(false);
            }
        });
    }

    @FXML
    void switchToLogin(MouseEvent event) {
        SceneController.setScene("login");
    }

    @FXML
    void register(MouseEvent event) {
        handleRegister();
    }

    boolean checkValidEmail(String email) {
        return email.matches("^\\S+@\\S+\\.\\S+$");
    }

    boolean checkValidUsername(String username) {
        return username.matches("^[A-Za-z0-9]{8,32}$");
    }

    boolean checkValidPassword(String password) {
        return password.matches("^(?=.*?[0-9])(?=.*?[A-Za-z]).{8,32}$");
    }

    void handleRegister() {
        List<TextField> data = List.of(name, username, email, password, confirmPassword);
        // Check for empty fields or fields with errors
        if (data.stream().anyMatch(field -> field.getText().isEmpty() || field.getStyleClass().contains("error"))) {
            alert.setVisible(true);
        } else {
            String res = AuthController.handleRegister(data);
            if (res != null) {
                alert.setVisible(true);
                alertText.setText(res);
            } else {
                // switch to chat screen
                SceneController.setScene("chat");
            }
        }
    }
}