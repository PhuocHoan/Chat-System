package com.haichutieu.chatsystem.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haichutieu.chatsystem.server.dal.CustomerService;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.server.util.HibernateUtil;
import com.haichutieu.chatsystem.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

public class SocketServer {
    final String HOST = "localhost";
    final int PORT = 8000;
    private final Map<Integer, AsynchronousSocketChannel> onlineUsers = new ConcurrentHashMap<>(); // Map<username, AsynchronousSocketChannel>
    private AsynchronousServerSocketChannel serverChannel;
    private final int bufferSize = 1024;

    private SocketServer() {
        try {
            HibernateUtil.getInstance();
            serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(HOST, PORT));
            System.out.println("Server started at " + HOST + ":" + PORT);
            // Accept connections
            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    // Accept the next connection
                    serverChannel.accept(null, this);
                    // Handle this connection
                    handleClient(clientChannel);
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
        return SocketServer.SocketServerHelper.INSTANCE;
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
                        String response = processInput(line, clientChannel);
                        if (response != null) {
                            if (response.getBytes().length <= bufferSize) {
                                clientChannel.write(ByteBuffer.wrap((response + "\n").getBytes())).get();
                            } else {
                                splitIntoChunks(response).forEach(chunk -> {
                                    ByteBuffer responseBuffer = ByteBuffer.wrap(chunk.getBytes());
                                    System.out.println("[SERVER] Sending: " + new String(responseBuffer.array()));
                                    clientChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                                        @Override
                                        public void completed(Integer result, ByteBuffer attachment) {
                                            if (attachment.hasRemaining()) {
                                                System.out.println("[SERVER] Sending: " + new String(attachment.array()));
                                                clientChannel.write(attachment, attachment, this);
                                            }
                                        }

                                        @Override
                                        public void failed(Throwable exc, ByteBuffer attachment) {
                                            exc.printStackTrace();
                                        }
                                    });
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        return switch (command) {
            case "REGISTER" -> handleRegister(content, clientChannel);
            case "LOGIN" -> handleLogin(content, clientChannel);
            case "SEARCH_USER" -> handleSearchUser(content);
            case "GET_FRIEND_LIST" -> handleGetFriendList(content);
            case "ADD_FRIEND" -> handleAddFriend(content);
            case "UNFRIEND" -> handleUnfriend(content);
            case "SPAM" -> handleSpamReport(content);
            case "BLOCK" -> handleBlock(content);
//            case "MESSAGE" -> // ex: MESSAGE username1 username2 hello world
//                    handleMessage(parts);
//            case "CREATE_GROUP" -> handleCreateGroup(parts);
//            case "GROUP_MESSAGE" -> handleGroupMessage(parts);
            case "OFFLINE" -> // user exits the app or logs out
                    handleOffline(content, clientChannel);
            default -> "ERROR Unknown command";
        };
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
        List<String> fields = Util.deserializeObject(parts[1], List.class);
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
        List<String> fields = Util.deserializeObject(parts[1], List.class);
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

    private String handleOffline(String username, AsynchronousSocketChannel clientChannel) {
        try {
            clientChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (username.equals("null")) {
            return null;
        }
        String[] parts = username.split(" ", 4);
        int id = Integer.parseInt(parts[1]);
        onlineUsers.remove(id);
        CustomerService.updateLogoutCustomer(id, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        return "OFFLINE User " + parts[0] + " is offline now END";
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
    private String handleGetFriendList(String userID) {
        int id = Integer.parseInt(userID);
        List<Customer> friends = null;
        friends = FriendsService.fetchFriends(id);

        if (friends == null) {
            return "GET_FRIEND_LIST ERROR Failed to fetch friends";
        }

        List<Integer> onlineUsers = new ArrayList<>();
        for (var entry : this.onlineUsers.entrySet()) {
            onlineUsers.add(entry.getKey());
        }

        String message = "GET_FRIEND_LIST OK " + Util.serializeObject(friends) + " END";
        if (!onlineUsers.isEmpty()) {
            message += " ONLINE " + Util.serializeObject(onlineUsers) + " END";
        }
        message += "\n";

        return message;
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

//    private String handleGroupMessage(String[] parts) {
//        if (parts.length < 3) {
//            return "ERROR Invalid GROUP_MESSAGE command\n";
//        }
//        String sender = parts[1];
//        String groupName = parts[2].split(" ", 2)[0];
//        String messageContent = parts[2].split(" ", 2)[1];
//        Group group = groups.get(groupName);
//        if (group != null) {
//            for (String member : group.getMembers()) {
//                if (!member.equals(sender)) {
//                    User recipientUser = onlineUsers.get(member);
//                    if (recipientUser != null) {
//                        sendMessageToClient(recipientUser.getChannel(), "GROUP_MESSAGE [" + groupName + "] " + sender + ": " + messageContent + "\n");
//                    }
//                }
//            }
//            return "OK Group message sent\n";
//        } else {
//            return "ERROR Group does not exist\n";
//        }
//    }

//    private void sendMessageToClient(AsynchronousSocketChannel channel, String message) {
//        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
//        channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
//            @Override
//            public void completed(Integer result, ByteBuffer attachment) {
//                // Successfully sent
//            }
//
//            @Override
//            public void failed(Throwable exc, ByteBuffer attachment) {
//                exc.printStackTrace();
//            }
//        });
//    }

    private static class SocketServerHelper {
        private static final SocketServer INSTANCE = new SocketServer();
    }
}

