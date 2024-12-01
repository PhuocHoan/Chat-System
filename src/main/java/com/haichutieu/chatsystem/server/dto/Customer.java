package com.haichutieu.chatsystem.dto;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "customer", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private int id;

    @Column(name = "username", nullable = false, unique = true, length = 32)
    private String username;

    @Column(name = "password", nullable = false, length = 32)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "sex", length = 50)
    private String sex;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "create_date", nullable = false)
    private Timestamp create_date;

    @Column(name = "is_lock", nullable = false)
    private boolean is_lock;

    public Customer() {
    }

    public long getId() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreate_date() {
        return create_date;
    }

    public void setCreate_date(Timestamp create_date) {
        this.create_date = create_date;
    }

    public boolean isIs_lock() {
        return is_lock;
    }

    public void setIs_lock(boolean is_lock) {
        this.is_lock = is_lock;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", birthdate=" + birthdate +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", create_date=" + create_date +
                ", is_lock=" + is_lock +
                '}';
    }
}