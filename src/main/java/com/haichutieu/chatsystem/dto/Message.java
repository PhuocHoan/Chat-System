package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "conversation_id", nullable = false)
    private long conversationID;

    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Column(name = "time", nullable = false)
    private Timestamp time;

    @Column(name = "message", nullable = false)
    private String message;

    public Message() {
    }

    public Message(Long conversationID, int customerID, Timestamp time, String message) {
        this.conversationID = conversationID;
        this.customerID = customerID;
        this.time = time;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getConversationID() {
        return conversationID;
    }

    public void setConversationID(long conversationID) {
        this.conversationID = conversationID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return conversationID == message.conversationID && customerID == message.customerID && time == message.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationID, customerID, time);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                "conversationID=" + conversationID +
                ", customerID=" + customerID +
                ", time=" + time +
                ", message='" + message + '\'' +
                '}';
    }
}
