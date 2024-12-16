package com.haichutieu.chatsystem.dto;

// for admin
public class MemberConversation {
    public int id;
    public String name;
    public String username;
    public boolean isAdmin;

    public MemberConversation() {
    }

    public MemberConversation(int id, String name, String username, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.username = username;
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
