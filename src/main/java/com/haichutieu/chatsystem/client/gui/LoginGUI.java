package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.UnaryOperator;

public class LoginGUI {

    private static LoginGUI instance;
    @FXML
    private HBox alert;
    @FXML
    private Text alertText;
    @FXML
    private Button button;
    @FXML
    private Label forgetPassword;
    @FXML
    private PasswordField password;
    @FXML
    private Label registerAccount;
    @FXML
    private VBox fieldContainer;
    @FXML
    private HBox screen;
    @FXML
    private TextField username;

    public LoginGUI() {
        instance = this;
    }

    public static LoginGUI getInstance() {
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

        // Display error fields for each input
        displayErrorField(username, "username", 2);
        displayErrorField(password, "password", 5);
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
                } else if (element.equals("password")) {
                    if (!checkValidPassword(field.getText())) {
                        field.getStyleClass().add("error");
                        error.setText("Password must be between 8 and 32 characters (A-Z, a-z, 0-9)");
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
    void login(MouseEvent event) {
        handleLogin();
    }

    @FXML
    void switchToRegister(MouseEvent event) {
        SceneController.setScene("register");
    }

    boolean checkValidUsername(String username) {
        return username.matches("^[A-Za-z0-9]{8,32}$");
    }

    boolean checkValidPassword(String password) {
        return password.matches("^(?=.*?[0-9])(?=.*?[A-Za-z]).{8,32}$");
    }

    void handleLogin() {
        List<String> fields = List.of(username.getText(), password.getText());
        if (username.getStyleClass().contains("error") || password.getStyleClass().contains("error")) {
            AuthController.handleLogin(fields, "error");
        } else {
            AuthController.handleLogin(fields, null);
        }
    }

    public void displayError(String message) {
        alertText.setText(message);
        alert.setVisible(true);
    }

    // receive login result from socket client
    public void loginResult(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("ERROR")) {
                displayError(message.replaceFirst("ERROR ", ""));
            } else {
                String[] parts = message.split(" END ", 2);
                Customer customer = Util.deserializeObject(parts[0], Customer.class);
                SessionManager.getInstance().setCurrentUser(customer);
                System.out.println(parts[1]);
                if (customer.getId() != SessionManager.getInstance().getCurrentUser().getId()) {
                    FriendGUI.getInstance().onUserOnline(customer.getId());
                }
                SceneController.setScene("chat");
            }
        });
    }
}
