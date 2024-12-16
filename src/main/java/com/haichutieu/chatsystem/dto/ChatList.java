package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class ChatList {
    public long conversationID;
    public String conversationName;
    public String senderName;
    public String latestMessage;
    public Timestamp latestTime;
    public boolean isGroup;
    public boolean isSeen; // status column

    public ChatList() {
    }

    public ChatList(long conversationID, String conversationName, String senderName, String latestMessage, Timestamp latestTime, boolean isGroup, boolean isSeen) {
        this.conversationID = conversationID;
        this.conversationName = conversationName;
        this.senderName = senderName;
        this.latestMessage = latestMessage;
        this.latestTime = latestTime;
        this.isGroup = isGroup;
        this.isSeen = isSeen;
    }

    public ChatList(ChatList chatList) {
        this.conversationID = chatList.conversationID;
        this.conversationName = chatList.conversationName;
        this.senderName = chatList.senderName;
        this.latestMessage = chatList.latestMessage;
        this.latestTime = chatList.latestTime;
        this.isGroup = chatList.isGroup;
        this.isSeen = chatList.isSeen;
    }

    @Override
    public String toString() {
        return "ChatList{" +
                "conversationId=" + conversationID +
                ", conversationName='" + conversationName + '\'' +
                ", senderName='" + senderName + '\'' +
                ", latestMessage='" + latestMessage + '\'' +
                ", latestTime=" + latestTime +
                ", isGroup=" + isGroup +
                ", isSeen=" + isSeen +
                '}';
    }
}
