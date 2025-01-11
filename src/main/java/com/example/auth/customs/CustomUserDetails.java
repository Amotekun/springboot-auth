package com.example.auth.customs;

import com.example.auth.entities.UserEntity;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final String email;
    private final String id;

    public CustomUserDetails(UserEntity userEntity, Collection<? extends GrantedAuthority> authorities) {
        super(userEntity.getUsername(), userEntity.getPassword(), authorities);
        this.email = userEntity.getEmail();
        this.id = userEntity.getId();
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

}
