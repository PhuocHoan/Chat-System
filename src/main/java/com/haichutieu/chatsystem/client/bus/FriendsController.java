package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;
import java.util.Set;

public class FriendsController {
    public static void fetchFriendList(String message) {
        List<Customer> friends = null;
        Set<Integer> onlineFriendIds = null;

        if (!message.startsWith("ERROR")) {
            String[] parts = message.split("END");
            String[] friendsJson = parts[0].trim().split(" ", 2);
            String[] onlineUserJson = parts[1].trim().split(" ", 2);
            friends = Util.deserializeObject(friendsJson[1], new TypeReference<>() {
            });
            onlineFriendIds = Util.deserializeObject(onlineUserJson[1], new TypeReference<>() {
            });
        }
        FriendGUI.getInstance().onReceiveFriendList(friends, onlineFriendIds);
    }

    public static void handleUnfriend(String message) {
        String[] parts = message.split(" ");
        int friendId = Integer.parseInt(parts[1]);
        if (parts[0].equals("ERROR")) {
            FriendGUI.getInstance().onUnfriendError(friendId);
        } else {
            FriendGUI.getInstance().onUnfriendSuccess(friendId);
        }
    }

    public static void handleAddFriend(String message) {
        FriendGUI.getInstance().onSendFriendRequest(!message.split(" ")[0].equals("ERROR"));
    }

    public static void handleSpam(String message) {
        FriendGUI.getInstance().onSpamResponse(!message.split(" ")[0].equals("ERROR"));
    }

    public static void handleBlock(String message) {
        String[] parts = message.split(" ");
        int friendId = Integer.parseInt(parts[1]);
        if (parts[0].equals("ERROR")) {
            FriendGUI.getInstance().onBlockError(friendId);
        } else {
            FriendGUI.getInstance().onBlockSuccess(friendId);
        }
    }

    public static void handleUserSearch(String message) {
        List<Customer> users;
        if (message.startsWith("ERROR")) {
            FriendGUI.getInstance().onUserSearch(false, null);
        } else {
            String data = message.split(" ", 2)[1];
            users = Util.deserializeObject(data, new TypeReference<>() {
            });
            FriendGUI.getInstance().onUserSearch(true, users);
        }
    }
}