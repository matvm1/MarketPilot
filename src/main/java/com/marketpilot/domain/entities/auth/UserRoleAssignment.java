package com.marketpilot.domain.entities.auth;

public record UserRoleAssignment(User user, Role role) {
    public UserRoleAssignment {
        if (user == null)
            throw new IllegalArgumentException("user cannot be null");
        if (role == null)
            throw new IllegalArgumentException("role cannot be null");
    }
}
