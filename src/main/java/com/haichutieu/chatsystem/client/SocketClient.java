package com.haichutieu.chatsystem.client;

import com.haichutieu.chatsystem.client.bus.AdminController;
import com.haichutieu.chatsystem.client.bus.AuthController;
import com.haichutieu.chatsystem.client.bus.ChatAppController;
import com.haichutieu.chatsystem.client.bus.FriendsController;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
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
            // Commands for USERS
            case "REGISTER":
                AuthController.handleRegister(parts[1]);
                break;
            case "LOGIN":
                AuthController.handleLogin(parts[1]);
                break;
            case "RESET_PASSWORD":
                AuthController.handleForgotPassword(parts[1]);
                break;
            case "CHAT_LIST":
                ChatAppController.handleChatList(parts[1]);
                break;
            case "GET_ONLINE_USERS":
                ChatAppController.handleOnlineUsers(parts[1]);
                break;
            case "GET_ALL_MEMBER_CONVERSATION":
                ChatAppController.handleAllMemberConversation(parts[1]);
                break;
            case "GET_MESSAGE_CONVERSATION":
                ChatAppController.handleMessageConversation(parts[1]);
                break;
            case "UPDATE_STATUS_CONVERSATION":
                System.out.println(parts[1]);
                break;
            case "MESSAGE":
                ChatAppController.receiveMessage(parts[1]);
                break;
            case "REMOVE_MESSAGE_ME":
                System.out.println(parts[1]);
                break;
            case "REMOVE_MESSAGE_ALL":
                ChatAppController.handleRemoveMessageAll(parts[1]);
                break;
            case "REMOVE_ALL_MESSAGE_ME":
                System.out.println(parts[1]);
                break;
            case "CREATE_GROUP":
                ChatAppController.handleCreateGroup(parts[1]);
                break;
            case "GROUP":
                ChatAppController.handleGroup(parts[1]);
                break;
            case "UPDATE_ACCOUNT":
                AuthController.handleUpdateAccount(parts[1]);
                break;
            case "OFFLINE":
                ChatAppController.handleOfflineUser(parts[1]);
                break;
            case "ONLINE":
                ChatAppController.handleGetOnlineUser(parts[1]);
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

            // Commands for ADMIN
            case "LOGIN_ADMIN":
                AdminController.handleLoginAdmin(parts[1]);
                break;
            case "FETCH_ACCOUNT_LIST":
                AdminController.fetchAccountList(parts[1]);
                break;
            case "FETCH_GROUP_LIST":
                AdminController.handleGroupList(parts[1]);
                break;
            case "FETCH_MEMBER_LIST":
                AdminController.handleMemberList(parts[1]);
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
                break;
            case "FETCH_ONLINE_USER_COUNT_LIST":
                AdminController.handleOnlineUserCountList(parts[1]);
                break;
            case "FETCH_NEW_USERS_MONTHLY":
                AdminController.handleNewUsersMonthly(parts[1]);
                break;
            case "FETCH_APP_USAGE_MONTHLY":
                AdminController.handleAppUsageMonthly(parts[1]);
                break;
        }
    }
}
