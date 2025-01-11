package com.example.auth.entities;

import java.time.Instant;
import java.util.UUID;

public class TwoFactorTokenEntity {
    private String id;
    private String userId;
    private String hashedOtp;
    private Instant expiresAt;
    private Integer attemptCount;

    public TwoFactorTokenEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public TwoFactorTokenEntity(String userId, String hashedOtp, Instant expiresAt) {
        this();
        this.userId = userId;
        this.hashedOtp = hashedOtp;
        this.expiresAt = expiresAt;
        this.attemptCount = 0;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setToken(String token) {
        this.hashedOtp = token;
    }

    public String getToken() {
        return hashedOtp;
    }

    public  Integer getAttemptCount() {return attemptCount;}


    public Instant getExpiresAt() {
        return expiresAt;
    }
}
