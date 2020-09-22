package com.dev.post.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long id;
    private String login;
    private String password;
    private List<Role> roles;
    private byte[] photo;
    private String fullName;
}
