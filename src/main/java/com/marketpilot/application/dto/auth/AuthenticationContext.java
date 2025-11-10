package com.marketpilot.application.dto.auth;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;

public record AuthenticationContext(User principal, Role role) {
    public AuthenticationContext {
        if (principal == null) {
            throw new IllegalArgumentException("principal cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
    }
}