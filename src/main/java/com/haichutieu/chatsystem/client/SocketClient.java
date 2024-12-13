package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.bus.ChatAppController;
import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.client.bus.AdminController;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.client.gui.LoginGUI;
import com.haichutieu.chatsystem.client.gui.SignupGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
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
            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            clientChannel.write(buffer).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        StringBuilder serverData = new StringBuilder();
        Thread.startVirtualThread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024); // allocate 1MB
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

    private void handleMessageFromServer(String messages) {
        String[] parts = messages.split(" ", 2);
        String command = parts[0];
        switch (command) {
            // Commands for USERS
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
            case "GET_MEMBER_CONVERSATION":
                getMemberConversation(parts[1]);
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
            case "GET_FRIEND_LIST":
                FriendsController.fetchFriendList(parts[1]);
                break;
            case "SEARCH_USER":
                FriendsController.handleUserSearch(parts[1]);
                break;
            case "ADD_FRIEND":
                FriendsController.handleAddFriend(parts[1]);
                break;
            case "GET_FRIEND_REQUEST":
                FriendsController.handleAcceptFriend(parts[1]);
                break;
            case "ANSWER_INVITATION":
                FriendsController.handleAnswerInvitation(parts[1]);
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

//            // Commands for ADMIN
            case "LOGIN_ADMIN":
                AdminController.handleLoginAdmin(parts[1]);
                break;
            case "FETCH_ACCOUNT_LIST":
                AdminController.fetchAccountList(parts[1]);
                break;
            case "ADD_ACCOUNT":
                UserManagement.getInstance().onAddNewAccount(parts[1]);
                break;
            case "DELETE_ACCOUNT":
                UserManagement.getInstance().onDeleteAccount(parts[1]);
                break;
            case "EDIT_ACCOUNT":
                UserManagement.getInstance().onEditAccount(parts[1]);
                break;
            case "LOGIN_HISTORY":
                AdminController.handleLoginHistory(parts[1]);
                break;
            case "TOGGLE_ACCOUNT_STATUS":
                AdminController.handleLockStatus(parts[1]);
                break;
            case "CHANGE_PASSWORD":
                AdminController.handleChangePassword(parts[1]);
                break;
            case "SPAM_LIST":
                AdminController.handleSpamList(parts[1]);
                break;
            case "LOCK_ACCOUNT":
                AdminController.handleLockAccount(parts[1]);
                break;
            case "FRIEND_COUNT":
                AdminController.handleFriendCount(parts[1]);
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

    private void getMemberConversation(String message) {
        ChatAppController.handleMemberConversation(message);
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
        System.out.println(message);
    }

    private void handleGetFriendList(String message) {
        FriendsController.fetchFriendList(message);
    }

    private void handleUnfriend(String message) {
        FriendsController.handleUnfriend(message);
    }

    private static class SocketClientHelper {
        private static final SocketClient INSTANCE = new SocketClient();
    }
}
