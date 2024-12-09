package com.haichutieu.chatsystem.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haichutieu.chatsystem.dto.UserLoginTime;
import com.haichutieu.chatsystem.server.dal.CustomerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.dto.MessageConversation;
import com.haichutieu.chatsystem.server.dal.CustomerService;
import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.server.dal.HistoryService;
import com.haichutieu.chatsystem.server.dal.HibernateUtil;
import com.haichutieu.chatsystem.server.dal.MessageService;
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
    private final int bufferSize = 1024;
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

    private void handleClient(AsynchronousSocketChannel clientChannel) {
        StringBuilder clientData = new StringBuilder();
        Thread.startVirtualThread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            try {
                while (clientChannel.read(buffer).get() != -1) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    buffer.clear();
                    String input = new String(bytes);

                    clientData.append(input);

                    String line;
                    while ((line = Util.readLine(clientData)) != null) {
                        System.out.println("[SERVER]: Received " + line);
                        String response = processInput(line, clientChannel);
                        if (response != null) {
                            if (response.getBytes().length <= bufferSize) {
                                System.out.println("[SERVER]: Sending " + response);
                                clientChannel.write(ByteBuffer.wrap((response + "\n").getBytes())).get();
                            } else {
                                splitIntoChunks(response).forEach(chunk -> {
                                    try {
                                        System.out.println("[SERVER]: Sending " + chunk);
                                        clientChannel.write(ByteBuffer.wrap(chunk.getBytes())).get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                clientChannel.write(ByteBuffer.wrap(("\n").getBytes())).get();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        });
    }

    private List<String> splitIntoChunks(String message) {
        List<String> chunks = new ArrayList<>();
//        String[] parts = message.split(" ", 3);
//        String content = message.substring(parts[0].length() + parts[1].length() + 2);

        // Convert to byte to count before splitting (because of UTF-8 format)
        byte[] bytes = message.getBytes();
        int numChunks = (int) Math.ceil((double) bytes.length / bufferSize);
//        chunks.add(parts[0] + " " + parts[1] + " " + numChunks);
        for (int i = 0; i < numChunks; i++) {
            int start = i * bufferSize;
            int end = Math.min(bytes.length, (i + 1) * bufferSize);
            chunks.add(new String(Arrays.copyOfRange(bytes, start, end)));
        }

        return chunks;
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
            case "SEARCH_USER":
                return handleSearchUser(content);
            case "ADD_FRIEND":
                return handleAddFriend(content);
            case "SPAM":
                return handleSpamReport(content);
            case "BLOCK":
                return handleBlock(content);
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
            case "GET_FRIEND_LIST":
                return handleGetFriendList(content);
            case "UNFRIEND":
                return handleUnfriend(content);
            case "OFFLINE":
                return handleOffline(content, clientChannel); // user exit or logout

            // Admin commands
            case "LOGIN_ADMIN":
                return handleAdminLogin(content);
            case "FETCH_ACCOUNT_LIST":
                return handleFetchAccountList();
            case "ADD_ACCOUNT":
                return handleAddAccount(content);
            case "DELETE_ACCOUNT":
                return handleDeleteAccount(content);
            case "EDIT_ACCOUNT":
                return handleEditAccount(content);
//            case "TOGGLE_ACCOUNT_STATUS" -> handleToggleAccountStatus(content);
//            case "CHANGE_PASSWORD" -> handleChangePassword(content);
            case "LOGIN_HISTORY":
                return handleLoginHistory(content);
//            case "USER_FRIEND_LIST" -> handleUserFriendList(content);
//            case "SPAM_LIST" -> handleSpamList(content);

            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
        return null;
    }

    private String handleAddFriend(String content) {
        String[] parts = content.split(" ");
        int userID = Integer.parseInt(parts[0]);
        int friendID = Integer.parseInt(parts[1]);
        if (!FriendsService.addFriend(userID, friendID)) {
            return "ADD_FRIEND ERROR " + friendID;
        }
        return "ADD_FRIEND OK " + friendID;
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
            message = Util.deserializeObject(content, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var memberConversation = MessageService.getMemberConversationUser(message.conversation_id);
        MessageService.removeMessage(message.id);
        String removeMessage = "REMOVE_MESSAGE_ALL " + Util.serializeObject(message);
        // send message to all members in same conversation
        assert memberConversation != null;
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

//    private String handleMessage(String[] parts) {
//        if (parts.length < 3) {
//            return "ERROR Invalid MESSAGE command\n";
//        }
//        String sender = parts[1];
//        String recipient = parts[2].split(" ", 2)[0];
//        String messageContent = parts[2].split(" ", 2)[1];
//        Customer recipientUser = onlineUsers.get(recipient);
//        if (recipientUser != null) {

    //            sendMessageToClient(recipientUser.getChannel(), "MESSAGE " + sender + ": " + messageContent + "\n");
//
//            return "OK Message sent\n";
//        } else {
//            return "ERROR User is not online\n";
//        }
//    }
    private String handleGetFriendList(String content) {
        String[] parts = content.split(" ", 2);
        int id = Integer.parseInt(parts[1]);

        List<Customer> friends = null;
        friends = FriendsService.fetchFriends(id);

        if (friends == null) {
            return "GET_FRIEND_LIST " + parts[0] + " ERROR Failed to fetch friends";
        }

        return "GET_FRIEND_LIST " + parts[0] + " OK" + (parts[0].equals("ADMIN") ? " " + id : "") + " " + Util.serializeObject(friends);
    }

    private String handleUnfriend(String message) {
        String[] parts = message.split(" ");
        int userID = Integer.parseInt(parts[0]);
        int friendID = Integer.parseInt(parts[1]);
        if (!FriendsService.removeFriend(userID, friendID)) {
            return "UNFRIEND ERROR " + friendID;
        }
        return "UNFRIEND OK " + friendID;
    }

    private String handleSearchUser(String content) {
        String[] parts = content.split(" ", 2);
        int username = Integer.parseInt(parts[0]);
        String prompt = parts[1];
        List<Customer> users = FriendsService.fetchUsers(username, prompt);
        if (users == null) {
            return "SEARCH_USER ERROR Failed to search user";
        }
        return "SEARCH_USER OK " + Util.serializeObject(users);
    }

    private String handleSpamReport(String content) {
        String[] parts = content.split(" ");
        int userID = Integer.parseInt(parts[0]);
        int reportedID = Integer.parseInt(parts[1]);
        if (!FriendsService.reportSpam(userID, reportedID)) {
            return "SPAM ERROR " + reportedID;
        }
        return "SPAM OK " + reportedID;
    }

    private String handleBlock(String message) {
        String[] parts = message.split(" ");
        int userID = Integer.parseInt(parts[0]);
        int blockedID = Integer.parseInt(parts[1]);
        if (!FriendsService.blockUser(userID, blockedID)) {
            return "BLOCK ERROR " + blockedID;
        }
        return "BLOCK OK " + blockedID;
    }

    private String handleAdminLogin(String content) {
        String[] parts = content.split(" ");
        String username = parts[0];
        String password = parts[1];
        if (username.equals("admin") && password.equals("admin")) {
            return "LOGIN_ADMIN OK";
        }
        return "LOGIN_ADMIN INCORRECT";
    }

    private String handleFetchAccountList() {
        List<Customer> allAccounts = CustomerService.fetchAllCustomers();
        return "FETCH_ACCOUNT_LIST OK " + Util.serializeObject(allAccounts);
    }

    private static class SocketServerHelper {
        private static final SocketServer INSTANCE = new SocketServer();
    }

    private String handleAddAccount(String content) {
        Customer cus = null;
        cus = Util.deserializeObject(content, new TypeReference<Customer>() {
        });

        if (CustomerService.getCustomerByUsername(cus.getUsername()) != null) {
            return "ADD_ACCOUNT EXISTS Username already exists!";
        }

        if (CustomerService.getCustomerByEmail(cus.getEmail()) != null) {
            return "ADD_ACCOUNT EXISTS Email already exists!";
        }

        if (!CustomerService.addCustomer(cus)) {
            return "ADD_ACCOUNT ERROR Server failed to add account";
        }

        Customer newAccount = CustomerService.getCustomerByUsername(cus.getUsername());
        return "ADD_ACCOUNT OK " + Util.serializeObject(newAccount);
    }

    private String handleDeleteAccount(String content) {
        int id = Integer.parseInt(content);
        if (!CustomerService.deleteCustomer(id)) {
            return "DELETE_ACCOUNT ERROR " + id;
        }
        return "DELETE_ACCOUNT OK " + id;
    }

    private String handleEditAccount(String content) {
        Customer cus = null;
        cus = Util.deserializeObject(content, new TypeReference<Customer>() {
        });

        Customer checkCus = CustomerService.getCustomerByUsername(cus.getUsername());
        if (checkCus != null && checkCus.getId() != cus.getId()) {
            return "EDIT_ACCOUNT ERROR Username already exists!";
        }

        checkCus = null;
        checkCus = CustomerService.getCustomerByEmail(cus.getEmail());
        if (checkCus != null && checkCus.getId() != cus.getId()) {
            return "EDIT_ACCOUNT ERROR Email already exists!";
        }

        if (!CustomerService.editCustomer(cus)) {
            return "EDIT_ACCOUNT ERROR Server failed to edit account";
        }

        return "EDIT_ACCOUNT OK " + content;
    }

    private String handleLoginHistory(String content) {
        if (content.startsWith("ALL")) {
            List<UserLoginTime> loginTimes = HistoryService.fetchAllLoginHistory();
            if (loginTimes == null) {
                return "LOGIN_HISTORY ALL ERROR";
            }
            return "LOGIN_HISTORY ALL OK " + Util.serializeObject(loginTimes);
        } else if (content.startsWith("USER")) {
            int id = Integer.parseInt(content.split(" ")[1]);
            List<LoginTime> loginTimes = HistoryService.fetchUserLoginHistory(id);
            if (loginTimes == null) {
                return "LOGIN_HISTORY USER ERROR";
            }
            return "LOGIN_HISTORY USER OK " + Util.serializeObject(loginTimes);
        }
        return "LOGIN_HISTORY ERROR Incorrect command";
    }
}

