package com.haichutieu.chatsystem.client.bus;

import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.dto.Customer;

import java.util.List;

public class FriendsController {

    private final FriendsService friendsService = new FriendsService();

    public List<Customer> fetchFriendList(int userID) {
        return friendsService.fetchFriends(userID);
    }
}
