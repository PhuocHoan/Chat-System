package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "login_time")
public class LoginTime {
    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Id
    @Column(name = "time", nullable = false)
    private Timestamp time;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline;

    @Column(name = "number_people_chat_with")
    private int numberPeopleChatWith;

    @Column(name = "number_group_chat_with")
    private int numberGroupChatWith;

    public LoginTime() {
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

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public int getNumberPeopleChatWith() {
        return numberPeopleChatWith;
    }

    public void setNumberPeopleChatWith(int numberPeopleChatWith) {
        this.numberPeopleChatWith = numberPeopleChatWith;
    }

    public int getNumberGroupChatWith() {
        return numberGroupChatWith;
    }

    public void setNumberGroupChatWith(int numberGroupChatWith) {
        this.numberGroupChatWith = numberGroupChatWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginTime loginTime = (LoginTime) o;
        return customerID == loginTime.customerID && time == loginTime.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerID, time);
    }

    @Override
    public String toString() {
        return "LoginTime{" +
                "customerID=" + customerID +
                ", time=" + time +
                ", isOnline=" + isOnline +
                ", numberPeopleChatWith=" + numberPeopleChatWith +
                ", numberGroupChatWith=" + numberGroupChatWith +
                '}';
    }
}
