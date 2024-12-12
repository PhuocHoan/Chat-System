package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.LoginGUI;
import com.haichutieu.chatsystem.client.gui.SignupGUI;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;

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
}
