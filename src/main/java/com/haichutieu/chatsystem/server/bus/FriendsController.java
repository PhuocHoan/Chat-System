package com.haichutieu.chatsystem.server.bus;

import com.haichutieu.chatsystem.server.dal.FriendsService;
import com.haichutieu.chatsystem.server.dto.Customer;

import java.util.List;

public class FriendsController {

    private final FriendsService friendsService = new FriendsService();

    public List<Customer> fetchFriendList(long userID) {
        return friendsService.fetchFriends(userID);
    }
}
