package com.dev.post.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role {
    private Long id;
    private RoleName roleName;

    public enum RoleName {
        USER, APICALL
    }
}
