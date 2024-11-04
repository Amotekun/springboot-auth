package com.example.auth.services;

import com.example.auth.entities.User;
import com.example.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenService {

    private final UserService userService;
    private final UserRepository userRepository;

    public TokenService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String username) {
        return userService.loadUserByUsername(username);
    }

    // REFRESH TOKEN SERVICE
    public void saveRefreshToken(String username, String refreshToken) {
        userRepository.updateRefreshToken(username, refreshToken);
    }

    public Optional<User> getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public void clearRefreshToken(String username) {
        userRepository.clearRefreshToken(username);
    }

}
