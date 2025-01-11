package com.example.auth.services;

import com.example.auth.entities.UserEntity;
import com.example.auth.repositories.UserRepository;
import com.example.auth.util.CookieUtility;
import com.example.auth.util.JwtTokenUtility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TokenService {

    private final UserRepository userRepository;
    private final JwtTokenUtility jwtTokenUtility;
    private final CookieUtility cookieUtility;

    public TokenService(
            UserRepository userRepository,
            JwtTokenUtility jwtTokenUtility,
            CookieUtility cookieUtility
    ) {
        this.userRepository = userRepository;
        this.jwtTokenUtility = jwtTokenUtility;
        this.cookieUtility = cookieUtility;
    }

    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No refresh token provided"));

        UserEntity user = getUserByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        String newAccessToken = jwtTokenUtility.generateToken(authentication);
        String newRefreshToken = generateAndSaveRefreshToken(user.getUsername());

        cookieUtility.setRefreshTokenCookie(newRefreshToken, response);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // REFRESH TOKEN SERVICE
    public String generateAndSaveRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        userRepository.updateRefreshToken(username, refreshToken);
        return refreshToken;
    }

    public Optional<UserEntity> getUserByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public void clearRefreshToken(String username) {
        userRepository.clearRefreshToken(username);
    }

}
