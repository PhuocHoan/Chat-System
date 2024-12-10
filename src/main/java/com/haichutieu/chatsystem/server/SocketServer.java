package com.haichutieu.chatsystem.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.MessageConversation;
import com.haichutieu.chatsystem.server.dal.CustomerService;
import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.server.dal.MessageService;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import com.haichutieu.chatsystem.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class SocketServer {
    final String HOST = "localhost";
    final int PORT = 8080;
    private final Map<Integer, AsynchronousSocketChannel> onlineUsers = new ConcurrentHashMap<>(); // Map<user_id, AsynchronousSocketChannel>
    private AsynchronousServerSocketChannel serverChannel;

    private SocketServer() {
        try {
            HibernateUtil.getInstance();
            serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(HOST, PORT));
            System.out.println("Server started at " + HOST + ":" + PORT);
            // Accept connections
            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    // Handle this connection
                    handleClient(clientChannel);
                    // Accept the next connection
                    serverChannel.accept(null, this);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    exc.printStackTrace();
                }
            });
            // Keep the main thread alive
            Thread.currentThread().join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketServer getInstance() {
        return SocketServerHelper.INSTANCE;
    }

    private String readLine(StringBuilder sb) {
        int index = sb.indexOf("\n");
        if (index != -1) {
            String line = sb.substring(0, index);
            sb.delete(0, index + 1); // Remove the line and the newline character
            return line;
        }
        return null;
    }

    private void handleClient(AsynchronousSocketChannel clientChannel) {
        StringBuilder clientData = new StringBuilder();
        Thread.startVirtualThread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1048576);
            try {
                while (clientChannel.read(buffer).get() != -1) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    buffer.clear();
                    String input = new String(bytes);

                    clientData.append(input);
                    String line;
                    while ((line = readLine(clientData)) != null) {
                        String response = processInput(line, clientChannel);
                        if (response != null) {
                            clientChannel.write(ByteBuffer.wrap((response + "\n").getBytes())).get();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String processInput(String input, AsynchronousSocketChannel clientChannel) {
        String[] parts = input.split(" ", 2);
        String command = parts[0];
        String content = parts[1];
        switch (command) {
            case "REGISTER":
                return handleRegister(content, clientChannel);
            case "LOGIN":
                return handleLogin(content, clientChannel);
            case "CHAT_LIST":
                return getChatList(content);
            case "GET_ONLINE_USERS":
                return getOnlineUsers();
            case "GET_MEMBER_CONVERSATION":
                return getMemberConversation(content);
            case "GET_ALL_MEMBER_CONVERSATION":
                return getAllMemberConversation(content);
            case "GET_MEMBER_CONVERSATION_ADMIN":
                return getMemberConversationAdmin(content);
            case "GET_MESSAGE_CONVERSATION":
                return getMessageConversation(content);
            case "UPDATE_STATUS_CONVERSATION":
                return updateStatusConversation(content);
            case "MESSAGE":
                handleMessage(content);
                break;
            case "REMOVE_MESSAGE_ME":
                return removeMessageMe(content);
            case "REMOVE_MESSAGE_ALL":
                removeMessageAll(content);
                break;
            case "REMOVE_ALL_MESSAGE_ME":
                return removeAllMessageMe(content);
//            case "ADD_FRIEND":
//                return handleAddFriend(parts);
            case "GET_FRIEND_LIST":
                return handleGetFriendList(content);
            case "UNFRIEND":
                return handleUnfriend(content);
//            case "CREATE_GROUP":
//              return handleCreateGroup(parts);
            case "OFFLINE":
                return handleOffline(content, clientChannel); // user exit or logout
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
        return null;
    }

    private String handleRegister(String user, AsynchronousSocketChannel clientChannel) {
        String[] parts = user.split(" ", 2);
        // If the text field still has class "error"
        if (Objects.equals(parts[0], "ERROR")) {
            return "REGISTER ERROR Please fill in the fields below.";
        }
        List<String> fields;
        try {
            fields = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (CustomerService.getCustomerByUsername(fields.get(1)) != null) {
            return "REGISTER ERROR Username already exists";
        }
        if (CustomerService.getCustomerByEmail(fields.get(2)) != null) {
            return "REGISTER ERROR Email already exists";
        }

        Customer customer = new Customer();
        customer.setName(fields.get(0));
        customer.setUsername(fields.get(1));
        customer.setEmail(fields.get(2));

        String password = fields.get(3);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        customer.setPassword(hashedPassword);

        customer.setCreateDate(new Timestamp(System.currentTimeMillis()));
        customer.setIsLock(false);
        customer.setAdmin(false);
        CustomerService.addCustomer(customer);

        String customerContent = Util.serializeObject(customer);

        onlineUsers.put(customer.getId(), clientChannel);
        return "REGISTER " + customerContent + " END User " + customer.getUsername() + " is online now";
    }

    private String handleLogin(String user, AsynchronousSocketChannel clientChannel) {
        String[] parts = user.split(" ", 2);
        // If the text field still has class "error"
        if (parts[0].equals("ERROR")) {
            return "LOGIN ERROR Please fill in the fields below.";
        }
        List<String> fields;
        try {
            fields = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Customer customer = CustomerService.getCustomerByUsername(fields.get(0));

        if (customer == null || !BCrypt.checkpw(fields.get(1), customer.getPassword())) {
            return "LOGIN ERROR The username or password you entered is incorrect.";
        }

        if (onlineUsers.containsKey(customer.getId())) {
            return "LOGIN ERROR User is already online";
        }

        String customerContent = Util.serializeObject(customer);
        LoginTime loginTime = new LoginTime();
        loginTime.setCustomerID(customer.getId());
        loginTime.setTime(new Timestamp(System.currentTimeMillis()));
        loginTime.setIsOnline(true);

        CustomerService.addLoginCustomer(loginTime);
        onlineUsers.put(customer.getId(), clientChannel);
        return "LOGIN " + customerContent + " END User " + customer.getUsername() + " is online now"; // END is defined as the end of the data sending
    }

    private String getChatList(String id) {
        List<ChatList> chatList = MessageService.getChatList(Integer.parseInt(id));
        return "CHAT_LIST " + Util.serializeObject(chatList);
    }

    private String getOnlineUsers() {
        return "GET_ONLINE_USERS " + Util.serializeObject(onlineUsers.keySet());
    }

    private String getMemberConversation(String user) {
        String[] parts = user.split(" ", 2);
        var memberConversation = MessageService.getMemberConversationUser(Long.parseLong(parts[0]));
        return "GET_MEMBER_CONVERSATION " + parts[0] + " " + Util.serializeObject(memberConversation);
    }

    private String getAllMemberConversation(String user) {
        String[] parts = user.split(" ", 2);
        List<Long> conversationIDs;
        try {
            conversationIDs = Util.deserializeObject(parts[0], new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<Long, List<Integer>> memberConversations = new HashMap<>();
        conversationIDs.forEach(conversationID -> memberConversations.put(conversationID, MessageService.getMemberConversationUser(conversationID, Integer.parseInt(parts[1]))));
        return "GET_ALL_MEMBER_CONVERSATION " + Util.serializeObject(memberConversations);
    }

    private String getMemberConversationAdmin(String message) {
        var memberConversation = MessageService.getMemberConversation(Long.parseLong(message));
        return "GET_MEMBER_CONVERSATION_ADMIN " + Util.serializeObject(memberConversation);
    }

    private String getMessageConversation(String user) {
        String[] parts = user.split(" ", 2);
        var messageConversation = MessageService.getMessageConversation(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        return "GET_MESSAGE_CONVERSATION " + Util.serializeObject(messageConversation);
    }

    private String updateStatusConversation(String user) {
        String[] parts = user.split(" ", 2);
        MessageService.updateStatusConversation(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        return "UPDATE_STATUS_CONVERSATION Update status conversation successfully";
    }

    private String handleOffline(String user, AsynchronousSocketChannel clientChannel) {
        try {
            clientChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (user.equals("null")) {
            return null;
        }
        String[] parts = user.split(" ", 4);
        int id = Integer.parseInt(parts[1]);
        onlineUsers.remove(id);
        CustomerService.updateLogoutCustomer(id, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        return "OFFLINE User " + parts[0] + " is offline now";
    }

//    private String handleAddFriend(String[] parts) {
//        if (parts.length < 3) {
//            return "ERROR Invalid ADD_FRIEND command\n";
//        }
//        String username = parts[1];
//        String friendUsername = parts[2];
//        if (!users.containsKey(friendUsername)) {
//            return "ERROR User does not exist\n";
//        }
//        friendships.get(username).add(friendUsername);
//        return "OK Friend added\n";
//    }

    private void handleMessage(String content) {
        String[] parts = content.split(" END ", 2);
        ChatList conversation;
        MessageConversation message;
        try {
            conversation = Util.deserializeObject(parts[0], new TypeReference<>() {
            });
            message = Util.deserializeObject(parts[1], new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var memberConversation = MessageService.getMemberConversationUser(message.conversation_id);
        // persist message to message table and message_display table

        message.id = MessageService.addMessage(message, memberConversation); // update message_id in MessageConversation

        String sendingMessage = "MESSAGE " + Util.serializeObject(conversation) + " END " + Util.serializeObject(message);
        // send message to all members in same conversation
        assert memberConversation != null;
        memberConversation.forEach(member -> {
            AsynchronousSocketChannel memberChannel = onlineUsers.get(member);
            if (memberChannel != null) {
                try {
                    memberChannel.write(ByteBuffer.wrap((sendingMessage + "\n").getBytes())).get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private String removeMessageMe(String content) {
        String[] parts = content.split(" ", 2);
        MessageService.removeMessage(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        return "REMOVE_MESSAGE_ME Remove message on your side successfully";
    }

    private void removeMessageAll(String content) {
        MessageConversation message;
        try {
            message = Util.deserializeObject(content, new TypeReference<MessageConversation>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var memberConversation = MessageService.getMemberConversationUser(message.conversation_id);
        MessageService.removeMessage(message.id);
        String removeMessage = "REMOVE_MESSAGE_ALL " + Util.serializeObject(message);
        // send message to all members in same conversation
        memberConversation.forEach(member -> {
            AsynchronousSocketChannel memberChannel = onlineUsers.get(member);
            if (memberChannel != null) {
                try {
                    memberChannel.write(ByteBuffer.wrap((removeMessage + "\n").getBytes())).get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private String removeAllMessageMe(String content) {
        String[] parts = content.split(" END ", 2);
        try {
            MessageService.removeAllMessage(Util.deserializeObject(parts[0], new TypeReference<>() {
            }), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "REMOVE_ALL_MESSAGE_ME Remove all message on your side successfully";
    }

    private String handleGetFriendList(String userID) {
        long id = Long.parseLong(userID);
        List<Customer> friends;
        friends = FriendsService.fetchFriends(id);

        if (friends == null) {
            return "GET_FRIEND_LIST ERROR Failed to fetch friends\n";
        }

        List<Integer> onlineUsers = new ArrayList<>();
        for (var entry : this.onlineUsers.entrySet()) {
            onlineUsers.add(entry.getKey());
        }

        String message = "GET_FRIEND_LIST " + Util.serializeObject(friends) + " END";
        if (!onlineUsers.isEmpty()) {
            message += " ONLINE " + Util.serializeObject(onlineUsers) + " END";
        }

        return message;
    }

    private String handleUnfriend(String message) {
        String[] parts = message.split(" ");
        long userID = Integer.parseInt(parts[0]);
        long friendID = Integer.parseInt(parts[1]);
        if (!FriendsService.removeFriend(userID, friendID)) {
            return "UNFRIEND ERROR " + friendID;
        }
        return "UNFRIEND OK " + friendID;
    }
//    private String handleCreateGroup(String[] parts) {
//        if (parts.length < 3) {
//            return "ERROR Invalid CREATE_GROUP command\n";
//        }
//        String groupName = parts[1];
//        String[] memberUsernames = parts[2].split(" ");
//        Set<String> members = new HashSet<>(Arrays.asList(memberUsernames));
//        Group group = new Group(groupName, members);
//        groups.put(groupName, group);
//        return "OK Group created\n";
//    }

    private static class SocketServerHelper {
        private static final SocketServer INSTANCE = new SocketServer();
    }
}

