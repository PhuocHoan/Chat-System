package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FriendsController {
    public static void fetchFriendList(String message) throws JsonProcessingException {
            List<Customer> friends = null;
            Set<Integer> onlineFriendIds = null;

            if (!message.startsWith("ERROR")) {
                String[] parts = message.split(" END ");
                ObjectMapper mapper = new ObjectMapper();
                friends = mapper.readValue(parts[0], new TypeReference<List<Customer>>() {});
                onlineFriendIds = mapper.readValue(parts[1].split(" ")[1], new TypeReference<Set<Integer>>() {});
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
