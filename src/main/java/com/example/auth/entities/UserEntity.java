package com.example.auth.entities;


import java.util.UUID;

public class UserEntity {


    private String id;
    private String email;
    private String username;
    private String password;
    private String role;
    private boolean isVerified;
    private String activationToken;
    private String refreshToken;
    private boolean twoFactorEnabled;

    public UserEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean getIsVerified () {
        return isVerified;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    };

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean twoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}



