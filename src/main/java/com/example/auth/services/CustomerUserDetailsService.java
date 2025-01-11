package com.example.auth.services;

import com.example.auth.customs.CustomUserDetails;
import com.example.auth.exception.UserIDNotFoundException;
import com.example.auth.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomerUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
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


}
