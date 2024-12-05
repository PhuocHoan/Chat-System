package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "is_group", nullable = false)
    private boolean isGroup;

    @Column(name = "create_date", nullable = false)
    private Timestamp createDate;

    public Conversation() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isGroup=" + isGroup +
                ", createDate=" + createDate +
                '}';
    }
}
