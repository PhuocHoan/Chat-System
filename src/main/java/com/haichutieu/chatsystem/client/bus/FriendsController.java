package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.dto.Customer;

import java.util.List;
import java.util.Set;

public class FriendsController {

    public static void fetchFriendList(String message) throws JsonProcessingException {
        List<Customer> friends = null;
        Set<Integer> onlineFriendIds = null;
        ObjectMapper mapper = new ObjectMapper();

        if (!message.startsWith("ERROR")) {
            String[] parts = message.split("END");
            String[] friendsJson = parts[0].trim().split(" ", 2);
            String[] onlineUserJson = parts[1].trim().split(" ", 2);
            friends = mapper.readValue(friendsJson[1], new TypeReference<List<Customer>>() {
            });
            onlineFriendIds = mapper.readValue(onlineUserJson[1], new TypeReference<Set<Integer>>() {
            });
        }
        FriendGUI.getInstance().onReceiveFriendList(friends, onlineFriendIds);
    }

    public static void handleUnfriend(String message) {
        String[] parts = message.split(" ");
        String status = parts[0];
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
        String status = parts[0];
        int friendId = Integer.parseInt(parts[1]);
        if (parts[0].equals("ERROR")) {
            FriendGUI.getInstance().onBlockError(friendId);
        } else {
            FriendGUI.getInstance().onBlockSuccess(friendId);
        }
    }

    public static void handleUserSearch(String message) {
        List<Customer> users = null;

        if (message.startsWith("ERROR")) {
            FriendGUI.getInstance().onUserSearch(false, users);
        } else {
            String data = message.split(" ", 2)[1];
            ObjectMapper mapper = new ObjectMapper();

            try {
                users = mapper.readValue(data, new TypeReference<List<Customer>>() {
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            FriendGUI.getInstance().onUserSearch(true, users);
        }
    }
}

