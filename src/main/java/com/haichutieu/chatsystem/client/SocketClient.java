package com.haichutieu.chatsystem.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.client.gui.LoginGUI;
import com.haichutieu.chatsystem.client.gui.SignupGUI;
import com.haichutieu.chatsystem.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SocketClient {
    private static SocketClient instance;
    private final AsynchronousSocketChannel clientChannel;

    private SocketClient() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 8000)).get();
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
            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            clientChannel.write(buffer).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        StringBuilder serverData = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
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
                buffer.clear();
                String response = new String(bytes);
                serverData.append(response);

                String line;
                while ((line = Util.readLine(serverData)) != null) {
                    handleMessage(line);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void receiveChunks(List<String> chunkBuffer) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                buffer.flip();
                String chunk = new String(buffer.array(), 0, buffer.limit());
                // Continue reading chunks until the end
                if (!chunk.contains("END")) {
                    System.out.println("Received chunk: " + chunk);
                    chunkBuffer.add(chunk); // Store the received chunk
                    buffer.clear();
                    receiveChunks(chunkBuffer);
                }
                // Remove "END" from the last chunk and there is no more to read
                else {
                    chunkBuffer.add(chunk.replace(" END", ""));
                    buffer.clear();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });

    }

    public String reassembleChunks(List<String> rawChunks) {
        StringBuilder jsonBuilder = new StringBuilder();
        for (String chunk : rawChunks) {
            jsonBuilder.append(chunk);
        }

        return jsonBuilder.toString();
    }

    private void handleMessage(String messages) {
        String[] parts = messages.split(" ", 2);
        String command = parts[0];
        switch (command) {
            case "REGISTER":
                handleRegister(parts[1]);
                break;
            case "LOGIN":
                handleLogin(parts[1]);
                break;
            case "OFFLINE":
                handleOffline(parts[1]);
                break;
            case "GET_FRIEND_LIST":
                try {
                    FriendsController.fetchFriendList(parts[1]);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                break;
            case "SEARCH_USER":
                FriendsController.handleUserSearch(parts[1]);
                break;
            case "ADD_FRIEND":
                FriendsController.handleAddFriend(parts[1]);
                break;
            case "UNFRIEND":
                FriendsController.handleUnfriend(parts[1]);
                break;
            case "SPAM":
                FriendsController.handleSpam(parts[1]);
                break;
            case "BLOCK":
                FriendsController.handleBlock(parts[1]);
                break;
        }
    }

    private void handleRegister(String message) {
        SignupGUI.getInstance().registerResult(message);
    }

    private void handleLogin(String message) {
        LoginGUI.getInstance().loginResult(message);
    }

    private void handleOffline(String message) {
        FriendGUI.getInstance().onUserOffline(Integer.parseInt(message.split(" ")[1]));
    }

    private static class SocketClientHelper {
        private static final SocketClient INSTANCE = new SocketClient();
    }
}
