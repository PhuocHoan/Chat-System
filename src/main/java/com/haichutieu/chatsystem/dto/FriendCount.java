package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class FriendCount {
    private int id;
    private String username;
    private Timestamp createdDate;
    private long friendCount;
    private long friendOfFriendsCount;

    public FriendCount() {}

    public FriendCount(int id, String username, Timestamp createdDate, long friendCount, long friendOfFriendsCount) {
        this.id = id;
        this.username = username;
        this.createdDate = createdDate;
        this.friendCount = friendCount;
        this.friendOfFriendsCount = friendOfFriendsCount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public long getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }

    public long getFriendOfFriendsCount() {
        return friendOfFriendsCount;
    }

    public void setFriendOfFriendsCount(int friendOfFriendsCount) {
        this.friendOfFriendsCount = friendOfFriendsCount;
    }
}
