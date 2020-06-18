package com.example.skychat.Model;

public class User {

    private String avatar, name, status, dateofbirth, gender;

    public User(String avatar, String name, String status) {
        this.avatar = avatar;
        this.name = name;
        this.status = status;
    }

    public User() {
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }


    public String getStatus() {
        return status;
    }
}
