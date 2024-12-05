package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

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

    public SpamList() {
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
