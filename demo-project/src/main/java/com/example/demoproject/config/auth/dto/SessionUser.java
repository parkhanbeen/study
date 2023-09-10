package com.example.demoproject.config.auth.dto;

import java.io.Serializable;

import com.example.demoproject.domain.user.User;
import lombok.Getter;

@Getter
public class SessionUser implements Serializable {
    private final String name;
    private final String email;
    private final String picture;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
