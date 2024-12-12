package com.haichutieu.chatsystem.dto;

// for admin
public class MemberConversation {
    public String name;
    public boolean isAdmin;

    public MemberConversation() {
    }

    public MemberConversation(String name, boolean isAdmin) {
        this.name = name;
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "MemberConversation{" +
                "name=" + name +
                ", isAdmin='" + isAdmin + '\'' +
                '}';
    }
}
