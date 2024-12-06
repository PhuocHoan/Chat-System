package com.haichutieu.chatsystem.server;

import com.haichutieu.chatsystem.dto.ChatList;
import com.haichutieu.chatsystem.server.dal.CustomerService;
import com.haichutieu.chatsystem.dto.Customer;
import com.haichutieu.chatsystem.dto.LoginTime;
import com.haichutieu.chatsystem.server.dal.MessageService;
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
    final int PORT = 8080;
    private final Map<Integer, AsynchronousSocketChannel> onlineUsers = new ConcurrentHashMap<>(); // Map<username, AsynchronousSocketChannel>
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
        return SocketServerHelper.INSTANCE;
    }

    private void handleClient(AsynchronousSocketChannel clientChannel) {
        Thread.startVirtualThread(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            try {
                while (clientChannel.read(buffer).get() != -1) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);
                    String input = new String(bytes);
                    buffer.clear();
                    String response = processInput(input, clientChannel);
                    if (response != null) {
                        clientChannel.write(ByteBuffer.wrap(response.getBytes())).get();
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
        return switch (command) {
            case "REGISTER" -> handleRegister(content, clientChannel);
            case "LOGIN" -> handleLogin(content, clientChannel);
            case "CHAT_LIST" -> handleChatList(content);
//            case "ADD_FRIEND" -> handleAddFriend(parts);
//            case "MESSAGE" -> // ex: MESSAGE username1 username2 hello world
//                    handleMessage(parts);
//            case "CREATE_GROUP" -> handleCreateGroup(parts);
//            case "GROUP_MESSAGE" -> handleGroupMessage(parts);
            case "OFFLINE" -> handleOffline(content, clientChannel); // user exit or logout
            default -> throw new IllegalStateException("Unexpected value: " + command);
        };
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

    private String handleChatList(String id) {
        List<ChatList> chatList = MessageService.getChatList(Integer.parseInt(id));
        return "CHAT_LIST " + Util.serializeObject(chatList);
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

//    private String handleMessage(String[] parts) {
//        if (parts.length < 3) {
//            return "ERROR Invalid MESSAGE command\n";
//        }
//        String sender = parts[1];
//        String recipient = parts[2].split(" ", 2)[0];
//        String messageContent = parts[2].split(" ", 2)[1];
//        Customer recipientUser = onlineUsers.get(recipient);
//        if (recipientUser != null) {

    /// /            sendMessageToClient(recipientUser.getChannel(), "MESSAGE " + sender + ": " + messageContent + "\n");
//
//            return "OK Message sent\n";
//        } else {
//            return "ERROR User is not online\n";
//        }
//    }

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

