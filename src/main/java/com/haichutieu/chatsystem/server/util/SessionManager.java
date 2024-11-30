package com.haichutieu.chatsystem.server.util;

import com.haichutieu.chatsystem.server.dto.Customer;

public class SessionManager {

    private static SessionManager instance;
    private Customer currentUser;

    // Private constructor for singleton pattern
    private SessionManager() {}

    // Get the singleton instance
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Set the current user
    public void setCurrentUser(Customer user) {
        this.currentUser = user;
    }

    // Get the current user
    public Customer getCurrentUser() {
        return currentUser;
    }

    // Clear the session (e.g., during logout)
    public void clearSession() {
        currentUser = null;
    }
}
