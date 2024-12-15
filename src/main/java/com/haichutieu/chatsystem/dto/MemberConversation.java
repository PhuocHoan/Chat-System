package com.haichutieu.chatsystem.dto;

// for admin
public class MemberConversation {
    private int id;
    private String name;
    private boolean isAdmin;

    public MemberConversation() {
    }

    public MemberConversation(int id, String name, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.isAdmin = isAdmin;
    }

    // Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "MemberConversation{" +
                "id=" + id +
                "name=" + name +
                ", isAdmin='" + isAdmin + '\'' +
                '}';
    }
}
