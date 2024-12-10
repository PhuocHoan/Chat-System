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
            String[] parts = message.split(" END ");
            try {
                friends = Util.deserializeObject(parts[0], new TypeReference<>() {
                });
                onlineFriendIds = Util.deserializeObject(parts[1].split(" ")[1], new TypeReference<>() {
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
}
