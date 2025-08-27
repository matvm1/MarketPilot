package application.entities;

import domain.entities.auth.Permission;
import domain.entities.auth.UserRoleAssignment;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class UserSession {
    private final UserRoleAssignment userRoleAssignment;
    private final Instant expirationTime;

    public UserSession(UserRoleAssignment userRoleAssignment, Instant sessionStart) {
        if (userRoleAssignment == null)
            throw new IllegalArgumentException("userRoleAssignment cannot be null");
        if (sessionStart == null)
            throw new IllegalArgumentException("sessionStart cannot be null");

        this.userRoleAssignment = userRoleAssignment;
        Duration sessionDuration = Duration.ofHours(4);
        expirationTime = sessionStart.plus(sessionDuration);
    }

    public Set<Permission> getPermissions() {
        return userRoleAssignment.getRole().getPermissions();
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expirationTime);
    }
}
