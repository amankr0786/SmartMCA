package com.study.smartmca;

public class User {
    public String name, email, mobile, state, college;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String mobile, String state, String college) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.state = state;
        this.college = college;
    }
}
