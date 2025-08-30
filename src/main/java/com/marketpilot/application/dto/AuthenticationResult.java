package com.marketpilot.application.dto;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;

public record AuthenticationResult(User principal, Role role) {
    public AuthenticationResult {
        if (principal == null) {
            throw new IllegalArgumentException("principal cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
    }
}