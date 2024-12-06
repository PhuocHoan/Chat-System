package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.gui.ChatGUI;
import com.haichutieu.chatsystem.client.gui.LoginGUI;
import com.haichutieu.chatsystem.client.gui.SignupGUI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class SocketClient {
    private static SocketClient instance;
    private final AsynchronousSocketChannel clientChannel;

    private SocketClient() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 8080)).get();
            System.out.println("Connected to the server.");
            System.out.println(clientChannel);
            // Start a thread to read messages from the server
            Thread.startVirtualThread(this::readMessages);

        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketClient getInstance() {
        return SocketClient.SocketClientHelper.INSTANCE;
    }

    public AsynchronousSocketChannel getClientChannel() {
        return clientChannel;
    }

    public void sendMessages(String message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap((message).getBytes());
            clientChannel.write(buffer).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1048576); // allocate 1MB
        try {
            while (clientChannel.isOpen()) {
                Integer bytesRead = clientChannel.read(buffer).get();
                if (bytesRead == -1) {
                    System.out.println("Server closed the connection.");
                    break;
                }
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                String message = new String(bytes);
                handleMessageFromServer(message);
                buffer.clear();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void handleMessageFromServer(String messages) {
        String[] parts = messages.split(" ", 2);
        String command = parts[0];
        switch (command) {
            case "REGISTER":
                handleRegister(parts[1]);
                break;
            case "LOGIN":
                handleLogin(parts[1]);
                break;
            case "CHAT_LIST":
                handleChatList(parts[1]);
                break;
            case "MESSAGE":
                handleMessage(parts[1]);
                break;
            case "OFFLINE":
                handleOffline(parts[1]);
                break;
        }
    }

    private void handleRegister(String message) {
        SignupGUI.getInstance().registerResult(message);
    }

    private void handleLogin(String message) {
        LoginGUI.getInstance().loginResult(message);
    }

    private void handleChatList(String message) {
        ChatGUI.getInstance().chatListResult(message);
    }

    private void handleMessage(String message) {
        ChatGUI.getInstance();
    }

    private void handleOffline(String message) {
        System.out.println(message);
    }

    private static class SocketClientHelper {
        private static final SocketClient INSTANCE = new SocketClient();
    }
}
