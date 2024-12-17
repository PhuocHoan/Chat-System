package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class MessageConversation {
    public long id; // message_id
    public long conversation_id;
    public Integer senderID;
    public String senderName;
    public Timestamp time;
    public String message;

    public MessageConversation() {
    }

    public MessageConversation(long conversation_id, Integer senderID, String senderName, Timestamp time, String message) {
        this.conversation_id = conversation_id;
        this.senderID = senderID;
        this.senderName = senderName;
        this.time = time;
        this.message = message;
    }

    public MessageConversation(long id, long conversation_id, Integer senderID, String senderName, Timestamp time, String message) {
        this.id = id;
        this.conversation_id = conversation_id;
        this.senderID = senderID;
        this.senderName = senderName;
        this.time = time;
        this.message = message;
    }

    public MessageConversation(long conversation_id, Timestamp time, String message) {
        this.conversation_id = conversation_id;
        this.time = time;
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageConversation{" +
                "id=" + id +
                "conversation_id=" + conversation_id +
                ", senderID='" + senderID + '\'' +
                ", senderName='" + senderName + '\'' +
                ", time='" + time + '\'' +
                ", message=" + message +
                '}';
    }
}
