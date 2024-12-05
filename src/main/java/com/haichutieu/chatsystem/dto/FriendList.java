package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "friend_list")
public class FriendList {
    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Id
    @Column(name = "friend_id", nullable = false)
    private int friendID;

    @Column(name = "is_friend", nullable = false)
    private boolean isFriend;

    public FriendList() {
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getFriendID() {
        return friendID;
    }

    public void setFriendID(int friendID) {
        this.friendID = friendID;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendList friendList = (FriendList) o;
        return customerID == friendList.customerID && friendID == friendList.friendID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerID, friendID);
    }

    @Override
    public String toString() {
        return "FriendList{" +
                "customerID=" + customerID +
                ", friendID=" + friendID +
                ", isFriend=" + isFriend +
                '}';
    }
}