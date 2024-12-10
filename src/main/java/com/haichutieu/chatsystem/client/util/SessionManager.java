package com.haichutieu.chatsystem.client.util;

import com.haichutieu.chatsystem.dto.Customer;

import java.util.List;

public class SessionManager {
    public static int numberPeopleChatWith;
    public static int numberGroupChatWith;
    public List<Integer> onlineUsers;

    // implement Bill Pugh singleton class
    private Customer currentUser;

    private SessionManager() {
        numberPeopleChatWith = 0;
        numberGroupChatWith = 0;
    }

    public static SessionManager getInstance() {
        return SessionManagerHelper.INSTANCE;
    }

    // Get the current user
    public Customer getCurrentUser() {
        return currentUser;
    }

    // Set the current user
    public void setCurrentUser(Customer user) {
        this.currentUser = user;
        numberPeopleChatWith = 0;
        numberGroupChatWith = 0;
    }

    private static class SessionManagerHelper {
        private static final SessionManager INSTANCE = new SessionManager();
    }
}
