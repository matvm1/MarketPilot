package com.marketpilot.application.dto.user;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Set;

public abstract class UserAbstractDTO {
    private final String username;
    private final Set<Role> roles;
    private final String firstName;
    private final String middleName;
    private final String lastName;

    public UserAbstractDTO(String username, Set<Role> roles, String firstName, String middleName, String lastName) {
        this.username = username;
        this.roles = roles;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public String getUsername() { return username; }
    public Set<Role> getRoles() { return roles; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public boolean isValid() {
        return username != null &&
                !username.isBlank() &&
                roles != null &&
                !roles.isEmpty() &&
                firstName != null &&
                !firstName.isBlank() &&
                middleName != null &&
                lastName != null &&
                !lastName.isBlank();
    }
}
