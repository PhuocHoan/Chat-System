package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "conversation_member")
public class ConversationMember {
    @Id
    @Column(name = "conversation_id", nullable = false)
    private long conversationID;

    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    public ConversationMember() {
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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationMember conversationMember = (ConversationMember) o;
        return conversationID == conversationMember.conversationID && customerID == conversationMember.customerID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationID, customerID);
    }

    @Override
    public String toString() {
        return "ConversationMember{" +
                "conversationID=" + conversationID +
                ", customerID=" + customerID +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
