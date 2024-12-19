package com.haichutieu.chatsystem.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "spam_list")
public class SpamList {
    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Id
    @Column(name = "person_id", nullable = false)
    private int personID;

    @Column(name = "time", nullable = false)
    private Timestamp time;

    private String username;
    private String userReported;
    private String email;
    private boolean isLocked;

    public SpamList() {
    }

    public SpamList(int userID, int personID, String username, String email, String userReported, Timestamp time) {
        this.customerID = userID;
        this.personID = personID;
        this.time = time;
        this.username = username;
        this.userReported = userReported;
        this.email = email;
        this.isLocked = false;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserReported() {
        return userReported;
    }

    public void setUserReported(String userReported) {
        this.userReported = userReported;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpamList spamList = (SpamList) o;
        return customerID == spamList.customerID && personID == spamList.personID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerID, personID);
    }

    @Override
    public String toString() {
        return "SpamList{" +
                "customerID=" + customerID +
                ", personID=" + personID +
                ", time=" + time +
                '}';
    }
}
