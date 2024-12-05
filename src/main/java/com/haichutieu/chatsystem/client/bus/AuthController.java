package com.haichutieu.chatsystem.client.bus;

import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;

public class AuthController {
    public static void handleRegister(List<String> fields, String error) {
        String user = Util.serializeObject(fields);
        if (error != null) {
            SocketClient.getInstance().sendMessages("REGISTER ERROR " + user); // connect to the server and send the message
        } else {
            SocketClient.getInstance().sendMessages("REGISTER NULL " + user);
        }
    }

    public static void handleLogin(List<String> fields, String error) {
        String user = Util.serializeObject(fields);
        if (error != null) {
            SocketClient.getInstance().sendMessages("LOGIN ERROR " + user); // connect to the server and send the message
        } else {
            SocketClient.getInstance().sendMessages("LOGIN NULL " + user); // connect to the server and send the message
        }
    }
}
