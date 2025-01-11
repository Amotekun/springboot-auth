package com.example.auth.services;

import com.example.auth.entities.UserEntity;
import com.example.auth.repositories.UserRepository;
import com.example.auth.util.CookieUtility;
import com.example.auth.util.JwtTokenUtility;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtility jwtTokenUtility;
    private final TokenService tokenService;
    private final CookieUtility cookieUtility;
    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtTokenUtility jwtTokenUtility,
            TokenService tokenService,
            CookieUtility cookieUtility,
            TwoFactorService twoFactorService,
            UserRepository userRepository

    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtility = jwtTokenUtility;
        this.tokenService = tokenService;
        this.cookieUtility = cookieUtility;
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
    }

    public Authentication authentication(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    public String generateTokenAndSetCookies(
            String username,
            Authentication authentication,
            HttpServletResponse response
    ) {
        String accessToken = jwtTokenUtility.generateToken(authentication);
        String refreshToken = tokenService.generateAndSaveRefreshToken(username);

        cookieUtility.setRefreshTokenCookie(refreshToken, response);

        return accessToken;
    }

    public String login(Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Authentication authentication = authentication(username, password);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

//        boolean isVerified = userRepository.isUserVerified(user.getUsername());
        if (!user.getIsVerified()) {
            throw new IllegalStateException("User is not verified. Please verify your account.");
        }

        if (user.twoFactorEnabled()) {
            twoFactorService.generateAndSendOtp(user.getId());
            throw new IllegalStateException("Two-factor authentication required. OPT sent to your phone");
        }

        return generateTokenAndSetCookies(username, authentication, response);
    }
}