package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class ChatList {
    public long conversationId;
    public String conversationName;
    public String senderName;
    public String latestMessage;
    public Timestamp latestTime;
    public boolean isGroup;

    public ChatList() {
    }
    
    ChatList(long conversationId, String conversationName, String senderName, String latestMessage, Timestamp latestTime, boolean isGroup) {
        this.conversationId = conversationId;
        this.conversationName = conversationName;
        this.senderName = senderName;
        this.latestMessage = latestMessage;
        this.latestTime = latestTime;
        this.isGroup = isGroup;
    }

    @Override
    public String toString() {
        return "ChatList{" +
                "conversationId=" + conversationId +
                ", conversationName='" + conversationName + '\'' +
                ", senderName='" + senderName + '\'' +
                ", latestMessage='" + latestMessage + '\'' +
                ", latestTime=" + latestTime +
                ", isGroup=" + isGroup +
                '}';
    }
}
