package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.ChatGUI;
import com.haichutieu.chatsystem.client.gui.FriendGUI;
import com.haichutieu.chatsystem.client.gui.adminPanel.UserManagement;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.util.Util;

import java.util.List;

public class FriendsController {
    public static void fetchFriendList(String message) {
        List<Customer> friends;

        String[] parts = message.split(" ", 3);

        if (parts[0].equals("ADMIN")) {
            if (parts[1].equals("ERROR")) {
                UserManagement.getInstance().onReceiveFriendList(false, 0, null);
            } else {
                int userId = Integer.parseInt(parts[2].split(" ", 2)[0]);
                String json = parts[2].split(" ", 2)[1];
                System.out.println(json);
                try {
                    friends = Util.deserializeObject(json, new TypeReference<>() {
                    });
                    UserManagement.getInstance().onReceiveFriendList(true, userId, friends);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if (parts[0].equals("USER")) {
            if (parts[1].equals("ERROR")) {
                FriendGUI.getInstance().onReceiveFriendList(false, null);
            } else {
                try {
                    friends = Util.deserializeObject(parts[2], new TypeReference<>() {
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
        } else if (parts[0].equals("OK")) {
            FriendGUI.getInstance().onUnfriendSuccess(friendId);
        } else {
            FriendGUI.getInstance().onUnfriendFrom(friendId);
        }
    }

    public static void handleAddFriend(String message) {
        String[] parts = message.split(" ", 2);
        if (message.startsWith("FROM")) {
            Customer friend = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
            FriendGUI.getInstance().onReceiveNewFriendRequest(friend);
            return;
        }

        FriendGUI.getInstance().onSendFriendRequest(!parts[0].equals("ERROR"));
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
        } else if (parts[0].equals("OK")) {
            List<Customer> friendRequests = Util.deserializeObject(parts[1], new TypeReference<>() {
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
                Customer friend = Util.deserializeObject(parts[2], new TypeReference<>() {
                });
                FriendGUI.getInstance().onAcceptStatus(true, friend);
            } else if (status.equals("FROM")) {
                Customer user = Util.deserializeObject(parts[2], new TypeReference<>() {
                });
                FriendGUI.getInstance().onAcceptInvitation(user);
            } else {
                FriendGUI.getInstance().onAcceptStatus(false, null);
            }
        } else {
            if (status.equals("FROM")) {
                FriendGUI.getInstance().onRejectInvitation(parts[2]);
                return;
            }

            int friendId = Integer.parseInt(parts[2]);
            FriendGUI.getInstance().onRejectStatus(!status.equals("ERROR"), friendId);
        }
    }

    public static void handleNewOnlineUser(String message) {
        int userId = Integer.parseInt(message);
        if (FriendGUI.getInstance() != null && SessionManager.getInstance() != null) {
            if (userId != SessionManager.getInstance().getCurrentUser().getId()) {
                FriendGUI.getInstance().onNewOnlineUser(Integer.parseInt(message));
            }
        }
    }

    public static void handleOfflineUser(String message) {
        int userId = Integer.parseInt(message);
        if (FriendGUI.getInstance() != null && SessionManager.getInstance() != null) {
            if (userId != SessionManager.getInstance().getCurrentUser().getId()) {
                FriendGUI.getInstance().onOfflineUser(Integer.parseInt(message));
            }
        }
    }

    public static void openChatWith(int userId, int friendId) {
        SocketClient.getInstance().sendMessages("OPEN_CHAT " + userId + " " + friendId);
    }

    public static void onOpenChatWith(String message) {
        String[] parts = message.split(" ", 3);
        int conversationId = Integer.parseInt(parts[1]);
        if (parts[0].equals("OK")) {
            // Check if the ChatList exists
            ChatList chat = ChatGUI.getInstance().conversations.stream().filter(c -> c.conversationID == conversationId).findFirst().orElse(null);
            if (chat != null) {
                ChatGUI.getInstance().openChatWith(chat, true);
            } else {
                // Create new ChatList
                ChatList newChat = Util.deserializeObject(parts[2], new TypeReference<>() {
                });
                ChatGUI.getInstance().openChatWith(newChat, false);
            }
        }
    }
}