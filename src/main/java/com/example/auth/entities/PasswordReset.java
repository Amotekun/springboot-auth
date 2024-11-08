package com.example.auth.entities;

import java.time.Instant;
import java.util.UUID;

public class PasswordReset {
    private String id;
    private String userId;
    private String token;
    private Instant expiresAt;

    public PasswordReset() {
        this.id = UUID.randomUUID().toString();
    }

    public PasswordReset(String userId, String token, Instant expiresAt) {
        this();
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(String user_id) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }


    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }


    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
