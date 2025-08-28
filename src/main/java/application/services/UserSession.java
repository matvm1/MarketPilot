package application.services;

import application.ports.Authentication;
import domain.entities.auth.Permission;
import domain.entities.auth.Role;
import domain.entities.auth.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class UserSession {
    private final User principal;
    private final Role sessionRole;
    private final Instant expirationTime;

    public UserSession(Authentication auth, Instant sessionStart) {
        if (auth == null)
            throw new IllegalArgumentException("auth cannot be null");
        if (sessionStart == null)
            throw new IllegalArgumentException("sessionStart cannot be null");

        this.principal = auth.principal();
        this.sessionRole = auth.role();
        Duration sessionDuration = Duration.ofHours(4);
        expirationTime = sessionStart.plus(sessionDuration);
    }

    public User getPrincipal() { return principal; }

    public Role getSessionRole() { return sessionRole; }

    public Set<Permission> getPermissions() {
        return sessionRole.getPermissions();
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expirationTime);
    }
}
