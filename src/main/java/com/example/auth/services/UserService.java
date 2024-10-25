package com.example.auth.services;


import com.example.auth.customs.CustomUserDetails;
import com.example.auth.entities.User;
import com.example.auth.exception.UserIDNotFoundException;
import com.example.auth.exception.UsernameAlreadyExistsException;
import com.example.auth.repositories.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found!"));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLES_" + user.getRole()));

        return new CustomUserDetails(user, authorities);
    }

    public UserDetails loadUserById(String id) throws RuntimeException {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserIDNotFoundException(id));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLES_" + user.getRole()));

        return new CustomUserDetails(user, authorities);
    }

    @Transactional
    public boolean userVerifiedTokenStatus(String token) {
        var verifyCheck = userRepository.findByActivationToken(token);
        if (verifyCheck.isPresent()) {
            userRepository.verifyUser(token);
            return true;
        }

        return false;
    }

    @Transactional
    public void verifyUserLogin(String email) {
        boolean isVerified = userRepository.isUserVerified(email);

        if (!isVerified) {
            throw new IllegalStateException("User account is not verified");
        }
    }

    @Transactional
    public String createUser(User user) {
        var existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setIsVerified(false);

        System.out.println("Generate token: " + activationToken);

        userRepository.save(user);

        System.out.println("Saved user with token: " + user.getActivationToken());

        return activationToken;
    }

}