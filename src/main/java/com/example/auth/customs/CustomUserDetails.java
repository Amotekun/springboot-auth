package com.example.auth.customs;

import com.example.auth.entities.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;


public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final String email;
    private final String id;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), user.getPassword(), authorities);
        this.email = user.getEmail();
        this.id = user.getId();
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

}
