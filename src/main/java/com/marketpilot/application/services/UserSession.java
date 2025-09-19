package com.marketpilot.application.services;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class UserSession {
    private final User principal;
    private final Role sessionRole;
    private final Instant expirationTime;
    //TODO: Consider a safer type for ids (UUID?)
    private int id;

    public UserSession(int sessionID, AuthenticationResult auth, Instant sessionStart) {
        if (sessionID <= 0)
            throw new IllegalArgumentException("sessionId must be a positive integer");
        if (auth == null)
            throw new IllegalArgumentException("auth cannot be null");
        if (sessionStart == null)
            throw new IllegalArgumentException("sessionStart cannot be null");

        this.id = sessionID;
        this.principal = auth.principal();
        this.sessionRole = auth.role();
        Duration sessionDuration = Duration.ofHours(4);
        expirationTime = sessionStart.plus(sessionDuration);
    }

    public int getId() { return id; }

    public User getPrincipal() { return principal; }

    public Role getSessionRole() { return sessionRole; }

    public Set<Permission> getPermissions() {
        return sessionRole.getPermissions();
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expirationTime);
    }
}
