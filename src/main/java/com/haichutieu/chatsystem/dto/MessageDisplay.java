package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "message_display")
public class MessageDisplay {
    @Id
    @Column(name = "message_id", nullable = false)
    private long messageID;

    @Id
    @Column(name = "customer_id", nullable = false)
    private long customerID;

    @Column(name = "status", nullable = false)
    private boolean status;

    public MessageDisplay() {
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public long getCustomerID() {
        return customerID;
    }

    public void setCustomerID(long customerID) {
        this.customerID = customerID;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDisplay messageDisplay = (MessageDisplay) o;
        return messageID == messageDisplay.messageID && customerID == messageDisplay.customerID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageID, customerID);
    }

    @Override
    public String toString() {
        return "MessageDisplay{" +
                "messageID=" + messageID +
                ", customerID=" + customerID +
                ", status=" + status +
                '}';
    }
}
