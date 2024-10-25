package com.example.auth.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final UserService userService;

    public TokenService(UserService userService) {
        this.userService = userService;
    }

    public UserDetails loadUserByUsername(String username) {
        return userService.loadUserByUsername(username);
    }
}
