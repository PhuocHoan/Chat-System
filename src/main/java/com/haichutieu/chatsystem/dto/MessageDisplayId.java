package com.haichutieu.chatsystem.dto;

import java.io.Serializable;
import java.util.Objects;

// for composite key in MessageDisplay
public class MessageDisplayId implements Serializable {
    private long message;
    private int customerID;

    public MessageDisplayId() {
    }

    public MessageDisplayId(long message, int customerID) {
        this.message = message;
        this.customerID = customerID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDisplayId that = (MessageDisplayId) o;
        return message == that.message && customerID == that.customerID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, customerID);
    }
}