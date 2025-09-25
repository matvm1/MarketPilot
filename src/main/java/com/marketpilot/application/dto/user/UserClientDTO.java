package com.marketpilot.application.dto.user;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Set;

public class UserClientDTO extends UserAbstractDTO {
    private final String email;

    public UserClientDTO(String username, Set<Role> roles, String email, String firstName, String middleName, String lastName) {
        super(username, roles, firstName, middleName, lastName);
        this.email = email;
    }

    public String getEmail() { return email; }
}