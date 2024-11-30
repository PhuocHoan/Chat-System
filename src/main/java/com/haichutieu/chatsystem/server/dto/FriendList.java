package com.haichutieu.chatsystem.server.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "friend_list")
public class FriendList {
    @Id
    @Column(name = "customer_id")
    private int customerID;

    @Id
    @Column(name = "friend_id")
    private int friendID;

    @Column(name = "is_friend")
    private boolean isFriend;

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
}