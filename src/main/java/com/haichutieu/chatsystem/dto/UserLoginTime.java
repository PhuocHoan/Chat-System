package com.haichutieu.chatsystem.dto;

import java.sql.Timestamp;

public class UserLoginTime {
    private int id;
    private String username;
    private String name;
    private Timestamp time;

    public UserLoginTime() {}

    public UserLoginTime(int id, String username, String name, Timestamp time) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "UserLoginTime{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", time=" + time +
                '}';
    }
}
