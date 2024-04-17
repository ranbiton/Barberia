package com.example.barberia.Models;

import java.util.List;

public class User {
    private String email;
    private String password;
    private String phone;
    private String userName;

    private boolean admin;

    public boolean isAdmin() {
        return admin;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String phone, String userName) {
        this.email = email;
        this.phone = phone;
        this.userName = userName;
        this.admin = false;
    }

    public String getPhone() {
        return phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User(){}
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
