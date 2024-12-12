package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.bus.ChatAppController;
import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SocketClient {
    private static SocketClient instance = null;
    private final AsynchronousSocketChannel clientChannel;

    private SocketClient() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 8080)).get();
            System.out.println("Connected to the server.");
            // Start a thread to read messages from the server
            Thread.startVirtualThread(this::readMessages);

        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
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
        Thread.startVirtualThread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1048576); // allocate 1MB
            try {
                while (clientChannel.read(buffer).get() != -1) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    String message = new String(bytes);
                    buffer.clear();

                    serverData.append(message);
                    String line;
                    while ((line = Util.readLine(serverData)) != null) {
                        handleMessageFromServer(line);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
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

    public void handleLogout() {
        try {
            clientChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        instance = null;
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
                getChatList(parts[1]);
                break;
            case "GET_ONLINE_USERS":
                getOnlineUsers(parts[1]);
                break;
            case "GET_ALL_MEMBER_CONVERSATION":
                getAllMemberConversation(parts[1]);
                break;
//            case "GET_MEMBER_CONVERSATION_ADMIN":
//                getMemberConversationAdmin(parts[1]);
//                break;
            case "GET_MESSAGE_CONVERSATION":
                getMessageConversation(parts[1]);
                break;
            case "UPDATE_STATUS_CONVERSATION":
                updateStatusConversation(parts[1]);
                break;
            case "MESSAGE":
                handleMessage(parts[1]);
                break;
            case "REMOVE_MESSAGE_ME":
                removeMessageMe(parts[1]);
                break;
            case "REMOVE_MESSAGE_ALL":
                removeMessageAll(parts[1]);
                break;
            case "REMOVE_ALL_MESSAGE_ME":
                removeAllMessageMe(parts[1]);
                break;
            case "OFFLINE":
                handleOffline(parts[1]);
                break;
            case "ONLINE":
                handleOnline(parts[1]);
                break;
            case "GET_FRIEND_LIST":
                FriendsController.fetchFriendList(parts[1]);
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
        AuthController.handleRegister(message);
    }

    private void handleLogin(String message) {
        AuthController.handleLogin(message);
    }

    private void getChatList(String message) {
        ChatAppController.handleChatList(message);
    }

    private void getOnlineUsers(String message) {
        ChatAppController.handleOnlineUsers(message);
    }

    private void getAllMemberConversation(String message) {
        ChatAppController.handleAllMemberConversation(message);
    }

//    private void getMemberConversationAdmin(String message) {
//        ChatAppController.handleMemberConversationAdminServer(message);
//    }

    private void getMessageConversation(String message) {
        ChatAppController.handleMessageConversation(message);
    }

    private void updateStatusConversation(String message) {
        System.out.println(message);
    }

    private void handleMessage(String message) {
        ChatAppController.receiveMessage(message);
    }

    private void removeMessageMe(String message) {
        System.out.println(message);
    }

    private void removeMessageAll(String message) {
        ChatAppController.handleRemoveMessageAll(message);
    }

    private void removeAllMessageMe(String message) {
        System.out.println(message);
    }

    private void handleOffline(String message) {
        ChatAppController.handleOfflineUser(message);
    }

    private void handleOnline(String message) {
        ChatAppController.handleGetOnlineUser(message);
    }

    private void handleGetFriendList(String message) {
        FriendsController.fetchFriendList(message);
    }

    private void handleUnfriend(String message) {
        FriendsController.handleUnfriend(message);
    }
}
