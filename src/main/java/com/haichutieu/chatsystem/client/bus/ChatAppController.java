package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.ChatGUI;
import com.haichutieu.chatsystem.client.util.SceneController;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.MemberConversation;
import com.haichutieu.chatsystem.dto.MessageConversation;
import com.haichutieu.chatsystem.util.Util;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ChatAppController {
    // send request Chat List to server
    public static void getChatList() {
        SocketClient.getInstance().sendMessages("CHAT_LIST " + SessionManager.getInstance().getCurrentUser().getId());
    }

    // handle respond Chat List from server
    public static void handleChatList(String message) {
        try {
            ChatGUI.getInstance().chatListResult(Util.deserializeObject(message, new TypeReference<>() {
            }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // get all online users at the first time user login successfully
    public static void getOnlineUsers() {
        SocketClient.getInstance().sendMessages("GET_ONLINE_USERS null");
    }

    public static void handleOnlineUsers(String message) {
        try {
            SessionManager.getInstance().onlineUsers = Util.deserializeObject(message, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // get members for all conversation
    public static void getAllMemberConversation() {
        SocketClient.getInstance().sendMessages("GET_ALL_MEMBER_CONVERSATION " + SessionManager.getInstance().getCurrentUser().getId());
    }

    public static void handleAllMemberConversation(String message) {
        Map<Long, List<Integer>> members;
        try {
            members = Util.deserializeObject(message, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChatGUI.getInstance().getAllMemberConversation(members);
    }

    // send message offline user
    public static void offlineUser() {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            SocketClient.getInstance().sendMessages("OFFLINE " + SessionManager.getInstance().getCurrentUser().getUsername() + " " + SessionManager.getInstance().getCurrentUser().getId() + " " + SessionManager.numberPeopleChatWith + " " + SessionManager.numberGroupChatWith);
        } else {
            SocketClient.getInstance().sendMessages("OFFLINE null");
        }
    }

    // receive an offline user from server
    public static void handleOfflineUser(String message) {
        SessionManager.getInstance().onlineUsers.remove(Integer.parseInt(message));
        FriendsController.handleOfflineUser(message);
    }

    // receive an online user from server
    public static void handleGetOnlineUser(String message) {
        SessionManager.getInstance().onlineUsers.add(Integer.parseInt(message));
        FriendsController.handleNewOnlineUser(message);
    }

    public static void getMessageConversation(long conversationID) {
        SocketClient.getInstance().sendMessages("GET_MESSAGE_CONVERSATION " + conversationID + " " + SessionManager.getInstance().getCurrentUser().getId());
    }

    // get messages in 1 conversation from server
    public static void handleMessageConversation(String message) {
        List<MessageConversation> messagesServer;
        try {
            messagesServer = Util.deserializeObject(message, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChatGUI.getInstance().getMessageConversation(messagesServer);
    }

    public static void updateStatusConversation(long conversationID) {
        SocketClient.getInstance().sendMessages("UPDATE_STATUS_CONVERSATION " + conversationID + " " + SessionManager.getInstance().getCurrentUser().getId());
    }

    public static void sendMessage(ChatList conversation, MessageConversation message) {
        SocketClient.getInstance().sendMessages("MESSAGE " + Util.serializeObject(conversation) + " END " + Util.serializeObject(message));
    }

    public static void receiveMessage(String message) {
        String[] parts = message.split(" END ", 2);
        ChatList conversation;
        MessageConversation messageReceive;
        try {
            conversation = Util.deserializeObject(parts[0], new TypeReference<>() {
            });
            messageReceive = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
            ChatGUI.getInstance().receiveMessage(conversation, messageReceive);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // remove on my side
    public static void removeMessage(Long messageID) {
        SocketClient.getInstance().sendMessages("REMOVE_MESSAGE_ME " + messageID + " " + SessionManager.getInstance().getCurrentUser().getId());
    }

    // remove for all members
    public static void removeMessage(MessageConversation message) {
        SocketClient.getInstance().sendMessages("REMOVE_MESSAGE_ALL " + Util.serializeObject(message));
    }

    // remove all message on my side
    public static void removeAllMessage(long conversationID) {
        SocketClient.getInstance().sendMessages("REMOVE_ALL_MESSAGE_ME " + conversationID + " END " + SessionManager.getInstance().getCurrentUser().getId());
    }

    // handle remove message for all members
    public static void handleRemoveMessageAll(String message) {
        String[] parts = message.split(" END ", 2);
        MessageConversation messageRemove;
        List<MessageConversation> messagesServer;
        try {
            messageRemove = Util.deserializeObject(parts[0], new TypeReference<>() {
            });
            messagesServer = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
            ChatGUI.getInstance().removeMessageAll(messageRemove, messagesServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleCreateGroup(String part) {
        String[] parts = part.split(" ", 2);
        Platform.runLater(() -> {
            if (part.startsWith("ERROR")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to create group.");
                alert.show();
            } else {
                try {
                    ChatList conversation;
                    conversation = Util.deserializeObject(parts[1], new TypeReference<>() {
                    });

                    //ChatGUI.getInstance().createGroup(conversation);
                    if (part.startsWith("OK")) {
                        MessageConversation message = new MessageConversation(conversation.conversationID, new Timestamp(System.currentTimeMillis()), "Group created");
                        conversation.latestMessage = message.message;
                        conversation.latestTime = message.time;
                        SocketClient.getInstance().sendMessages("MESSAGE " + Util.serializeObject(conversation) + " END " + Util.serializeObject(message));
                        SceneController.setScene("chat");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void addGroupMember(ChatList chatList, String json, List<Customer> members) {
        SocketClient.getInstance().sendMessages("GROUP ADD_MEMBER " + chatList.conversationID + " " + json);

        StringBuilder sb = new StringBuilder();
        for (Customer member : members) {
            sb.append(member.getUsername()).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" was added to group");

        MessageConversation message = new MessageConversation(chatList.conversationID, new Timestamp(System.currentTimeMillis()), sb.toString());
        message.senderID = null;
        SocketClient.getInstance().sendMessages("MESSAGE " + Util.serializeObject(chatList) + " END " + Util.serializeObject(message));
    }

    public static void removeGroupMember(ChatList conversation, MemberConversation member) {
        SocketClient.getInstance().sendMessages("GROUP REMOVE_MEMBER " + conversation.conversationID + " " + member.getId());

        MessageConversation message = new MessageConversation(conversation.conversationID, new Timestamp(System.currentTimeMillis()), member.getUsername() + " left or was removed from group");
        message.senderID = null;
        SocketClient.getInstance().sendMessages("MESSAGE " + Util.serializeObject(conversation) + " END " + Util.serializeObject(message));
    }

    public static void updateGroupName(long conversationID, String text) {
        SocketClient.getInstance().sendMessages("GROUP UPDATE_NAME " + conversationID + " " + text);
    }

    public static void assignGroupAdmin(long conversationID, int id, boolean b) {
        SocketClient.getInstance().sendMessages("GROUP ASSIGN_ADMIN " + conversationID + " " + id + " " + b);
    }

    public static void getAllMemberConversationCard(long conversationID) {
        SocketClient.getInstance().sendMessages("GROUP GET_MEMBERS " + conversationID + " null");
    }

    public static void handleGroup(String part) {
        String[] parts = part.split(" ", 4);
        String status = parts[1];
        long conversationID = Long.parseLong(parts[2]);

        if (status.equals("ERROR")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                switch (parts[0]) {
                    case "ADD_MEMBER":
                        alert.setHeaderText("Failed to add member to group.");
                        break;
                    case "REMOVE_MEMBER":
                        alert.setHeaderText("Failed to remove member from group.");
                        break;
                    case "UPDATE_NAME":
                        alert.setHeaderText("Failed to update group name.");
                        break;
                    case "ASSIGN_ADMIN":
                        alert.setHeaderText("Failed to assign admin.");
                        break;
                    default:
                        return;
                }
                alert.show();
            });
            return;
        }

        List<MemberConversation> members;
        switch (parts[0]) {
            case "ADD_MEMBER":
                members = Util.deserializeObject(parts[3], new TypeReference<>() {
                });
                ChatGUI.getInstance().onAddGroupMember(conversationID, members);
                break;
            case "REMOVE_MEMBER":
                if (status.equals("FROM")) {
                    ChatGUI.getInstance().onRemoveGroupMember(conversationID);
                    return;
                }

                ChatGUI.getInstance().onRemoveGroupMember(conversationID, Util.deserializeObject(parts[3], new TypeReference<>() {
                }));
                break;
            case "UPDATE_NAME":
                ChatGUI.getInstance().onUpdateGroupName(conversationID, parts[3]);
                break;
            case "GET_MEMBERS":
                members = Util.deserializeObject(parts[3], new TypeReference<>() {
                });
                ChatGUI.getInstance().onGetConversationMembers(conversationID, members);
                break;
            case "ASSIGN_ADMIN":
                members = Util.deserializeObject(parts[3], new TypeReference<>() {
                });
                ChatGUI.getInstance().onAssignGroupAdmin(conversationID, members);
                break;
        }
    }

    public static void reportSpam(int userId, long conversationID) {
        SocketClient.getInstance().sendMessages("SPAM_CONVERSATION " + userId + " " + conversationID);
    }

    public static void onReportSpam(String message) {
        if (message.startsWith("ERROR")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to report spam.");
                alert.show();
            });
            return;
        }

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Spam");
            alert.setHeaderText("Report spam successfully.");
            alert.show();
        });
    }
}
