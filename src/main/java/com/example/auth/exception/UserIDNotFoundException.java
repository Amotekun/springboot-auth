package com.example.auth.exception;

public class UserIDNotFoundException extends RuntimeException {
    public UserIDNotFoundException(String id) {
        super("User not found with ID: " + id);
    }
}
