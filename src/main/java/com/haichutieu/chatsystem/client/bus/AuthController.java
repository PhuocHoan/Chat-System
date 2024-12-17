package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.AccountGUI;
import com.haichutieu.chatsystem.client.gui.ForgotPasswordGUI;
import com.haichutieu.chatsystem.client.gui.LoginGUI;
import com.haichutieu.chatsystem.client.gui.SignupGUI;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;

public class AuthController {
    // send register message to server
    public static void sendRegister(List<String> fields, String error) {
        String user = Util.serializeObject(fields);
        if (error != null) {
            SocketClient.getInstance().sendMessages("REGISTER ERROR " + user); // connect to the server and send the message
        } else {
            SocketClient.getInstance().sendMessages("REGISTER NULL " + user);
        }
    }

    // handle login message back from server
    public static void handleRegister(String message) {
        if (message.startsWith("ERROR")) {
            SignupGUI.getInstance().registerResult(message);
        } else {
            String[] parts = message.split(" END ", 2);
            Customer customer;
            try {
                customer = Util.deserializeObject(parts[0], new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            SessionManager.getInstance().setCurrentUser(customer);
            SignupGUI.getInstance().registerResult(parts[1]);
        }
    }

    // send login message to server
    public static void sendLogin(List<String> fields, String error) {
        String user = Util.serializeObject(fields);
        if (error != null) {
            SocketClient.getInstance().sendMessages("LOGIN ERROR " + user); // connect to the server and send the message
        } else {
            SocketClient.getInstance().sendMessages("LOGIN NULL " + user); // connect to the server and send the message
        }
    }

    // handle login message back from server
    public static void handleLogin(String message) {
        if (message.startsWith("ERROR")) {
            LoginGUI.getInstance().loginResult(message);
        } else {
            String[] parts = message.split(" END ", 2);
            Customer customer;
            try {
                customer = Util.deserializeObject(parts[0], new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            SessionManager.getInstance().setCurrentUser(customer);
            LoginGUI.getInstance().loginResult(parts[1]);
        }
    }

    // send register message to server
    public static void updateAccount(Customer c) {
        SocketClient.getInstance().sendMessages("UPDATE_ACCOUNT " + Util.serializeObject(c));
    }

    // handle login message back from server
    public static void handleUpdateAccount(String message) {
        if (message.startsWith("ERROR")) {
            AccountGUI.getInstance().onUpdateAccount(message);
        } else {
            String[] parts = message.split(" END ", 2);
            Customer customer;
            try {
                customer = Util.deserializeObject(parts[0], new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (customer.getUsername() != null) {
                SessionManager.getInstance().getCurrentUser().setUsername(customer.getUsername());
            }
            if (customer.getPassword() != null) {
                SessionManager.getInstance().getCurrentUser().setPassword(customer.getPassword());
            }
            if (customer.getName() != null) {
                SessionManager.getInstance().getCurrentUser().setName(customer.getName());
            }
            if (customer.getAddress() != null) {
                SessionManager.getInstance().getCurrentUser().setAddress(customer.getAddress());
            }
            if (customer.getBirthdate() != null) {
                SessionManager.getInstance().getCurrentUser().setBirthdate(customer.getBirthdate());
            }
            if (customer.getSex() != null) {
                SessionManager.getInstance().getCurrentUser().setSex(customer.getSex());
            }
            if (customer.getEmail() != null) {
                SessionManager.getInstance().getCurrentUser().setEmail(customer.getEmail());
            }
            AccountGUI.getInstance().onUpdateAccount(parts[1]);
        }
    }

    // send register message to server
    public static void forgotPassword(String email, String error) {
        if (error != null) {
            SocketClient.getInstance().sendMessages("RESET_PASSWORD ERROR " + email); // connect to the server and send the message
        } else {
            SocketClient.getInstance().sendMessages("RESET_PASSWORD NULL " + email);
        }
    }

    // handle login message back from server
    public static void handleForgotPassword(String message) {
        ForgotPasswordGUI.getInstance().forgotPasswordResult(message);

        // if current scene is adminPanel
        if (SceneController.primaryStage.getScene().equals(SceneController.scenes.get("adminPanel"))) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reset password");
                alert.setHeaderText("Password reset! Check user email for new password.");
                alert.show();
            });
        }
    }
}
