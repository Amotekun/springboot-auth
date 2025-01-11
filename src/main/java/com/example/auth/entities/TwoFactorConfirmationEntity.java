package com.example.auth.entities;

import java.util.UUID;

public class TwoFactorConfirmationEntity {
    private String id;
    private String userId;

    public TwoFactorConfirmationEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public TwoFactorConfirmationEntity(String userId) {
        this();
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
