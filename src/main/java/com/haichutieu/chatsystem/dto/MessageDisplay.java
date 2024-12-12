package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "message_display")
@IdClass(MessageDisplayId.class) // Composite primary key
public class MessageDisplay {

    @Id
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Column(name = "status", nullable = false)
    private boolean status;

    public MessageDisplay() {
    }

    public MessageDisplay(Message message, int customerID, boolean status) {
        this.message = message;
        this.customerID = customerID;
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
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
        MessageDisplay that = (MessageDisplay) o;
        return Objects.equals(message.getId(), that.message.getId()) && customerID == that.customerID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message.getId(), customerID);
    }

    @Override
    public String toString() {
        return "MessageDisplay{" +
                "messageID=" + message.getId() +
                ", customerID=" + customerID +
                ", status=" + status +
                '}';
    }
}