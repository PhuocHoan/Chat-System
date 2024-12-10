package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.util.SceneController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.function.UnaryOperator;

public class SignupGUI {

    private static SignupGUI instance;
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

    public SignupGUI() {
        instance = this;
    }

    public static SignupGUI getInstance() {
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
                handleRegister();
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
        List<String> fields = List.of(name.getText(), username.getText(), email.getText(), password.getText());
        if (name.getStyleClass().contains("error") || username.getStyleClass().contains("error") || email.getStyleClass().contains("error") || password.getStyleClass().contains("error") || confirmPassword.getStyleClass().contains("error")) {
            AuthController.sendRegister(fields, "error");
        } else {
            AuthController.sendRegister(fields, null);
        }
    }

    public void displayError(String message) {
        alertText.setText(message);
        alert.setVisible(true);
    }

    public void registerResult(String message) {
        Platform.runLater(() -> {
            if (!message.startsWith("ERROR")) {
                try {
                    SceneController.addScene("chat", "gui/chat.fxml", "../stylesheets/style.css");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                SceneController.setScene("chat");
                System.out.println(message);
            } else {
                displayError(message.replaceFirst("ERROR", ""));
            }
        });
    }
}