package com.haichutieu.chatsystem.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.haichutieu.chatsystem.dto.*;
import com.haichutieu.chatsystem.server.dal.*;
import com.haichutieu.chatsystem.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.Message;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
            case "RESET_PASSWORD":
                return handleForgotPassword(content);
            case "SEARCH_USER":
                return handleSearchUser(content);
            case "ADD_FRIEND":
                return handleAddFriend(content);
            case "GET_FRIEND_REQUEST":
                return handleGetFriendRequest(content);
            case "ANSWER_INVITATION":
                return handleAnswerInvitation(content);
            case "SPAM":
                return handleSpamReport(content);
            case "BLOCK":
                return handleBlock(content);
            case "CHAT_LIST":
                return getChatList(content);
            case "GET_ONLINE_USERS":
                return getOnlineUsers();
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
            case "UPDATE_ACCOUNT":
                return updateAccount(content);
            case "GET_FRIEND_LIST":
                return handleGetFriendList(content);
            case "UNFRIEND":
                return handleUnfriend(content);
            case "OFFLINE":
                handleOffline(content, clientChannel); // user exit or logout
                break;
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
            case "TOGGLE_ACCOUNT_STATUS":
                return handleToggleLockAccount(content);
            case "CHANGE_PASSWORD":
                return handleChangePassword(content);
//            case "RESET_PASSWORD":
//                return handleResetPassword(content);
            case "LOGIN_HISTORY":
                return handleLoginHistory(content);
//            case "USER_FRIEND_LIST" -> handleUserFriendList(content);
            case "SPAM_LIST":
                return handleSpamList();
            case "LOCK_ACCOUNT":
                return handleLockAccount(content);
            case "FRIEND_COUNT":
                return handleFriendCount();

            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
        return null;
    }

    private String handleAnswerInvitation(String content) {
        String[] parts = content.split(" ");
        String type = parts[0];
        int userID = Integer.parseInt(parts[1]);
        int friendID = Integer.parseInt(parts[2]);

        Customer friend = CustomerService.getCustomerByID(friendID);

        if (type.equals("ACCEPT")) {
            if (!FriendsService.acceptFriend(userID, friendID)) {
                return "ANSWER_INVITATION ACCEPT ERROR " + friendID;
            }
            return "ANSWER_INVITATION ACCEPT OK " + Util.serializeObject(friend);
        } else if (type.equals("REJECT")) {
            if (!FriendsService.rejectFriend(userID, friendID)) {
                return "ANSWER_INVITATION REJECT ERROR " + friendID;
            }
            return "ANSWER_INVITATION REJECT OK " + friendID;
        }

        return "ANSWER_INVITATION ERROR Invalid command";
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
        if (parts[0].equals("ERROR")) {
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
        String message = "ONLINE " + customer.getId();
        onlineUsers.forEach((userID, channel) -> {
            try {
                channel.write(ByteBuffer.wrap((message + "\n").getBytes())).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

        if (customer.getIsLock()) {
            return "LOGIN ERROR This account is locked.";
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
        String message = "ONLINE " + customer.getId();
        onlineUsers.forEach((userID, channel) -> {
            try {
                channel.write(ByteBuffer.wrap((message + "\n").getBytes())).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return "LOGIN " + customerContent + " END User " + customer.getUsername() + " is online now"; // END is defined as the end of the data sending
    }

    private String handleForgotPassword(String message) {
        String[] parts = message.split(" ", 2);
        // If the text field still has class "error"
        if (parts[0].equals("ERROR")) {
            return "RESET_PASSWORD ERROR Please fill in the field below.";
        }
        String emailAddress = parts[1];
        Customer c = CustomerService.getCustomerByEmail(emailAddress);
        if (c == null) {
            return "RESET_PASSWORD ERROR Email does not exist";
        }
        String randomPassword = randomPassword();
        String hashedPassword = BCrypt.hashpw(randomPassword, BCrypt.gensalt(10));
        c.setPassword(hashedPassword);
        if (!CustomerService.editCustomer(c)) {
            return "RESET_PASSWORD ERROR There is an error when reset password";
        }
        sendEmailResetPassword(emailAddress, randomPassword);
        return "RESET_PASSWORD Reset password successfully";
    }

    // send mail to user email to reset password
    void sendEmailResetPassword(String emailAddress, String content) {
        Properties props = new Properties();
        try (InputStream input = HibernateUtil.class.getClassLoader().getResourceAsStream("com/haichutieu/chatsystem/config.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String emailUsername = props.getProperty("email.username");
        String emailPassword = props.getProperty("email.password");

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.host", "smtp.gmail.com");
        mailProps.put("mail.smtp.port", "587");
        Session session = Session.getInstance(mailProps, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@2chutieu.com", "2chutieu.com Admin"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress, "user"));
            msg.setSubject("[2 chu tieu chat application][Reset Password]");
            msg.setText("Your new password is: " + content);
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // random password with length = 8 and each character is (A-Z, a-z, 0-9)
    String randomPassword() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 8;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String getChatList(String id) {
        List<ChatList> chatList = MessageService.getChatList(Integer.parseInt(id));
        return "CHAT_LIST " + Util.serializeObject(chatList);
    }

    private String getOnlineUsers() {
        return "GET_ONLINE_USERS " + Util.serializeObject(onlineUsers.keySet());
    }

    private String getAllMemberConversation(String user) {
        int userID = Integer.parseInt(user);
        List<Long> conversationIDs = MessageService.getAllConversation(userID);
        Map<Long, List<Integer>> memberConversations = new HashMap<>();
        assert conversationIDs != null;
        conversationIDs.forEach(conversationID -> memberConversations.put(conversationID, MessageService.getMemberConversationUser(conversationID, userID)));
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

    private void handleOffline(String user, AsynchronousSocketChannel clientChannel) {
        try {
            clientChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (user.equals("null")) {
            return;
        }
        String[] parts = user.split(" ", 4);
        int id = Integer.parseInt(parts[1]);
        onlineUsers.remove(id);
        CustomerService.updateLogoutCustomer(id, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        String message = "OFFLINE " + id;
        onlineUsers.forEach((userID, channel) -> {
            try {
                channel.write(ByteBuffer.wrap((message + "\n").getBytes())).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
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

    private String updateAccount(String user) {
        Customer c;
        try {
            c = Util.deserializeObject(user, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (CustomerService.getCustomerByUsername(c.getUsername()) != null) {
            return "UPDATE_ACCOUNT ERROR Username already exists";
        }
        if (CustomerService.getCustomerByEmail(c.getEmail()) != null) {
            return "UPDATE_ACCOUNT ERROR Email already exists";
        }

        if (c.getPassword() != null) {
            String password = c.getPassword();
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
            c.setPassword(hashedPassword);
        }

        if (CustomerService.updateCustomer(c)) {
            return "UPDATE_ACCOUNT " + Util.serializeObject(c) + " END " + "Update account successfully";
        } else {
            return "UPDATE_ACCOUNT ERROR There is error in update account";
        }
    }

    private String handleGetFriendList(String content) {
        String[] parts = content.split(" ", 2);
        int id = Integer.parseInt(parts[1]);

        List<Customer> friends;
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

    private String handleAddAccount(String content) {
        Customer cus;
        cus = Util.deserializeObject(content, new TypeReference<>() {
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
        Customer cus;
        cus = Util.deserializeObject(content, new TypeReference<>() {
        });

        Customer checkCus = CustomerService.getCustomerByUsername(cus.getUsername());
        if (checkCus != null && checkCus.getId() != cus.getId()) {
            return "EDIT_ACCOUNT ERROR Username already exists!";
        }

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

    private String handleToggleLockAccount(String content) {
        int id = Integer.parseInt(content);
        if (!CustomerService.toggleLockStatusAccount(id)) {
            return "TOGGLE_ACCOUNT_STATUS ERROR " + id;
        }
        return "TOGGLE_ACCOUNT_STATUS OK " + id;
    }

    private String handleChangePassword(String content) {
        String[] parts = content.split(" ", 2);
        int id = Integer.parseInt(parts[0]);
        String password = parts[1];

        if (!CustomerService.changePassword(id, password)) {
            return "CHANGE_PASSWORD ERROR " + id;
        }
        return "CHANGE_PASSWORD OK " + id;
    }

    private String handleSpamList() {
        List<SpamList> spamList = AdminService.fetchAllSpamList();
        if (spamList == null) {
            return "SPAM_LIST ERROR";
        }
        return "SPAM_LIST OK " + Util.serializeObject(spamList);
    }

    private String handleLockAccount(String id) {
        int userID = Integer.parseInt(id);
        if (!CustomerService.toggleLockStatusAccount(userID)) {
            return "LOCK_ACCOUNT ERROR " + userID;
        }
        return "LOCK_ACCOUNT OK " + userID;
    }

    private String handleFriendCount() {
        List<FriendCount> friendCountList = AdminService.fetchFriendCountList();
        if (friendCountList == null) {
            return "FRIEND_COUNT ERROR null";
        }
        return "FRIEND_COUNT OK " + Util.serializeObject(friendCountList);
    }

    private String handleGetFriendRequest(String content) {
        int id = Integer.parseInt(content);
        List<Customer> friendRequests = FriendsService.fetchFriendRequests(id);
        if (friendRequests == null) {
            return "GET_FRIEND_REQUEST ERROR Failed to fetch friend requests";
        }
        return "GET_FRIEND_REQUEST OK " + Util.serializeObject(friendRequests);
    }

    private static class SocketServerHelper {
        private static final SocketServer INSTANCE = new SocketServer();
    }
}

