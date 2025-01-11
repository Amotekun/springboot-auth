package com.example.auth.util;

public class ApiResponse {
    public String message;
    public Object data;

    public ApiResponse(String message) {
        this.message = message;
    }

    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;

    }
}
