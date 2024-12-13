package com.haichutieu.chatsystem.client.gui;

import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Date;

public class AccountGUI {
    private static AccountGUI instance;
    @FXML
    private TextField address;

    @FXML
    private DatePicker birthdate;

    @FXML
    private Button discard;

    @FXML
    private TextField email;

    @FXML
    private ImageView friendsBtn;

    @FXML
    private TextField name;

    @FXML
    private PasswordField password;

    @FXML
    private Button save;

    @FXML
    private GridPane screen;

    @FXML
    private TextField sex;

    @FXML
    private TextField username;

    public AccountGUI() {
        instance = this;
    }

    public static AccountGUI getInstance() {
        return instance;
    }

    @FXML
    void logout() {
        ChatGUI.getInstance().logout();
    }

    @FXML
    public void switchToChatTab() {
        SceneController.setScene("chat");
    }

    @FXML
    void switchToFriendsTab() {
        SceneController.setScene("friends");
    }

    @FXML
    public void initialize() {
        // Make the screen (container) focusable
        screen.setFocusTraversable(true);
        screen.setOnMouseClicked(event -> {
            screen.requestFocus();
        });

        save.setOnMouseClicked(event -> {
            Customer c = onChange();
            if (c != null) {
                c.setId(SessionManager.getInstance().getCurrentUser().getId());
                AuthController.updateAccount(c);
            }
        });

        discard.setOnMouseClicked(event -> displayAccountDetail());

        displayAccountDetail();
    }

    Customer onChange() {
        Customer c = new Customer();
        boolean flag = false;
        if (!username.getText().isEmpty() && !username.getText().equals(SessionManager.getInstance().getCurrentUser().getUsername())) {
            c.setUsername(username.getText());
            flag = true;
            if (!SignupGUI.getInstance().checkValidUsername(username.getText())) {
                alertError("Username must be between 8 and 32 characters (A-Z, a-z, 0-9)");
                return null;
            }
        }
        if (!password.getText().isEmpty() && !BCrypt.checkpw(password.getText(), SessionManager.getInstance().getCurrentUser().getPassword())) {
            c.setPassword(password.getText());
            flag = true;
            if (!SignupGUI.getInstance().checkValidPassword(password.getText())) {
                alertError("Password must be between 8 and 32 characters (A-Z, a-z, 0-9)");
                return null;
            }
        }
        if (!email.getText().isEmpty() && !email.getText().equals(SessionManager.getInstance().getCurrentUser().getEmail())) {
            c.setEmail(email.getText());
            flag = true;
            if (!SignupGUI.getInstance().checkValidPassword(email.getText())) {
                alertError("Email is not valid");
                return null;
            }
        }
        if (!name.getText().isEmpty() && !name.getText().equals(SessionManager.getInstance().getCurrentUser().getName())) {
            c.setName(name.getText());
            flag = true;
        }
        if (!address.getText().isEmpty() && !address.getText().equals(SessionManager.getInstance().getCurrentUser().getAddress())) {
            c.setAddress(address.getText());
            flag = true;
        }
        if (!sex.getText().isEmpty() && !sex.getText().equals(SessionManager.getInstance().getCurrentUser().getSex())) {
            c.setSex(sex.getText());
            flag = true;
        }
        if (birthdate.getValue() != null && !birthdate.getValue().equals(SessionManager.getInstance().getCurrentUser().getBirthdate() != null ? SessionManager.getInstance().getCurrentUser().getBirthdate().toLocalDate() : null)) {
            c.setBirthdate(Date.valueOf(birthdate.getValue()));
            flag = true;
        }
        if (!flag) {
            alertError("There is no update");
            return null;
        }
        return c;
    }

    void alertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Update Account");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    void alertSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Account");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    void displayAccountDetail() {
        username.setText(SessionManager.getInstance().getCurrentUser().getUsername());
        name.setText(SessionManager.getInstance().getCurrentUser().getName());
        password.clear();
        String userAddress = SessionManager.getInstance().getCurrentUser().getAddress();
        if (userAddress != null) {
            address.setText(userAddress);
        }
        birthdate.setValue(null);
        Date userBirthdate = SessionManager.getInstance().getCurrentUser().getBirthdate();
        if (userBirthdate != null) {
            birthdate.setValue(userBirthdate.toLocalDate());
        }
        String userSex = SessionManager.getInstance().getCurrentUser().getSex();
        if (userSex != null) {
            sex.setText(userSex);
        }

        email.setText(SessionManager.getInstance().getCurrentUser().getEmail());
    }

    public void onUpdateAccount(String message) {
        Platform.runLater(() -> {
            if (!message.startsWith("ERROR")) {
                alertSuccess(message);
                displayAccountDetail();
            } else {
                alertError(message.replaceFirst("ERROR", ""));
            }
        });
    }
}
