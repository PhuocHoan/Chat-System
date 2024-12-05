package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "block_list")
public class BlockList {
    @Id
    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Id
    @Column(name = "person_id", nullable = false)
    private int personID;

    public BlockList() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockList blockList = (BlockList) o;
        return customerID == blockList.customerID && personID == blockList.personID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerID, personID);
    }

    @Override
    public String toString() {
        return "BlockList{" +
                "customerID=" + customerID +
                ", personID=" + personID +
                '}';
    }
}
