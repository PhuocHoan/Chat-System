package com.haichutieu.chatsystem.client.bus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.client.SocketClient;
import com.haichutieu.chatsystem.client.gui.ChatGUI;
import com.haichutieu.chatsystem.client.util.SessionManager;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.MessageConversation;
import com.haichutieu.chatsystem.util.Util;

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

    // get member for all conversation
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
    }

    // receive an online user from server
    public static void handleGetOnlineUser(String message) {
        SessionManager.getInstance().onlineUsers.add(Integer.parseInt(message));
    }

////     for admin
//    public static void getMemberConversationAdmin(long conversationID) {
//        SocketClient.getInstance().sendMessages("GET_MEMBER_CONVERSATION_ADMIN " + conversationID);
//    }

    /// /     for admin
//    public static void handleMemberConversationAdmin(String message) {
//        ChatGUI.getInstance().getMemberConversationAdmin(message);
//    }
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
    public static void removeAllMessage(ChatList conversation) {
        SocketClient.getInstance().sendMessages("REMOVE_ALL_MESSAGE_ME " + Util.serializeObject(conversation) + " END " + SessionManager.getInstance().getCurrentUser().getId());
    }

    // handle remove message for all members
    public static void handleRemoveMessageAll(String message) {
        MessageConversation messageRemove;
        try {
            messageRemove = Util.deserializeObject(message, new TypeReference<>() {
            });
            ChatGUI.getInstance().removeMessageAll(messageRemove);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
