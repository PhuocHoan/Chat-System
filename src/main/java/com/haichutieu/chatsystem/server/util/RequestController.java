package com.haichutieu.chatsystem.server.util;

import com.haichutieu.chatsystem.server.bus.FriendsController;
import com.haichutieu.chatsystem.server.dto.Customer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;

public class RequestController {
    public static void handleRequest(ClientRequest request) {
        try {
            String clientRequest = request.getRequest();
            AsynchronousSocketChannel clientChannel = request.getClientChannel();

            String requestType = clientRequest.split(" ")[0];
            switch (requestType) {
                case "GET_FRIEND_LIST":
                    String userID = clientRequest.split(" ")[1];
                    FriendsController friendsController = new FriendsController();
                    List<Customer> friendsList = friendsController.fetchFriendList(Long.parseLong(userID));
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonResponse = objectMapper.writeValueAsString(friendsList);
//                ByteBuffer responseBuffer = ByteBuffer.wrap(jsonResponse.getBytes());
                    clientChannel.write(ByteBuffer.wrap("here is your list haha".getBytes()));
                    break;
                case "GET_CONVERSATION_LIST":
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
