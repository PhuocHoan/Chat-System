package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;
import java.util.Set;

public class FriendsController {
    public static void fetchFriendList(String message) {
        List<Customer> friends = null;
        Set<Integer> onlineFriendIds = null;

        String[] parts = message.split(" ", 3);
        ObjectMapper mapper = new ObjectMapper();

        if(parts[0].equals("ADMIN")) {
            if (parts[1].equals("ERROR")) {
                UserManagement.getInstance().onReceiveFriendList(false, 0, null);
            } else {
                int userId = Integer.parseInt(parts[2].split(" ", 2)[0]);
                String json = parts[2].split(" ", 2)[1];
                System.out.println(json);
                try {
                    friends = mapper.readValue(json, new TypeReference<List<Customer>>() {
                    });
                    UserManagement.getInstance().onReceiveFriendList(true, userId, friends);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if(parts[0].equals("USER")) {
            if (parts[1].equals("ERROR")) {
                FriendGUI.getInstance().onReceiveFriendList(false, null);
            } else {
                try {
                    friends = mapper.readValue(parts[2], new TypeReference<List<Customer>>() {
                    });
                    FriendGUI.getInstance().onReceiveFriendList(true, friends);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
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

    public static void handleAcceptFriend(String part) {
        String[] parts = part.split(" ", 2);
        if (parts[0].equals("ERROR")) {
            FriendGUI.getInstance().onReceiveFriendRequestList(false, null);
        } else {
            List<Customer> friendRequests = Util.deserializeObject(parts[1], new TypeReference<List<Customer>>() {
            });
            FriendGUI.getInstance().onReceiveFriendRequestList(true, friendRequests);
        }
    }

    public static void handleAnswerInvitation(String part) {
        String[] parts = part.split(" ", 3);
        String type = parts[0];
        String status = parts[1];

        if (type.equals("ACCEPT")) {
            if (status.equals("OK")) {
                Customer friend = Util.deserializeObject(parts[2], new TypeReference<Customer>() {
                });
                FriendGUI.getInstance().onAcceptStatus( true, friend);
            } else {
                FriendGUI.getInstance().onAcceptStatus(false, null);
            }
        } else {
            int friendId = Integer.parseInt(parts[2]);
            FriendGUI.getInstance().onRejectStatus(!status.equals("ERROR"), friendId);
        }
    }
}