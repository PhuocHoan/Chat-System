package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class OnlineUserCount {
    private int id;
    private String name;
    private Timestamp createdDate;
    private long loginTimes;
    private long numberPeopleChatWith;
    private long numberGroupChatWith;

    public OnlineUserCount() {
    }

    public OnlineUserCount(int id, String name, Timestamp createdDate, long loginTimes, long numberPeopleChatWith, long numberGroupChatWith) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.loginTimes = loginTimes;
        this.numberPeopleChatWith = numberPeopleChatWith;
        this.numberGroupChatWith = numberGroupChatWith;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public long getLoginTimes() {
        return loginTimes;
    }

    public void setLoginTimes(int loginTimes) {
        this.loginTimes = loginTimes;
    }

    public long getNumberPeopleChatWith() {
        return numberPeopleChatWith;
    }

    public void setNumberPeopleChatWith(int numberPeopleChatWith) {
        this.numberPeopleChatWith = numberPeopleChatWith;
    }

    public long getNumberGroupChatWith() {
        return numberGroupChatWith;
    }

    public void setNumberGroupChatWith(int numberGroupChatWith) {
        this.numberGroupChatWith = numberGroupChatWith;
    }

    @Override
    public String toString() {
        return "OnlineUserCount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdDate=" + createdDate +
                ", loginTimes=" + loginTimes +
                ", numberPeopleChatWith=" + numberPeopleChatWith +
                ", numberGroupChatWith=" + numberGroupChatWith +
                '}';
    }
}
