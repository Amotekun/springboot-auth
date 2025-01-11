package com.example.auth.entities;

import java.util.UUID;

public class UserContactInfoEntity {

    private final String id;
    private final String userId;
    private final String phoneNumber;
    private boolean phoneVerified;

    public UserContactInfoEntity(String userId, String phoneNumber) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = false;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }
}
