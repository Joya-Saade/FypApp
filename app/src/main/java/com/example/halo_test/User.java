package com.example.halo_test;

public class User {
    public String fullName, email;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }
}
