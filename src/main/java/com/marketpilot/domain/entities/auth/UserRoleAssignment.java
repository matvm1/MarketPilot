package com.marketpilot.domain.entities.auth;

public class UserRoleAssignment {
    private final User user;
    private final Role role;
    private boolean isActive;

    public UserRoleAssignment(User user, Role role) {
        if (user == null)
            throw new IllegalArgumentException("user cannot be null");
        if (role == null)
            throw new IllegalArgumentException("role cannot be null");
        this.user = user;
        this.role = role;
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    // returns true if active state was toggled
    public boolean setActive() {
        if (isActive)
            return false;

        isActive = true;
        return true;
    }

    // returns true if active state was toggled
    public boolean setInactive() {
        if (!isActive)
            return false;

        isActive = false;
        return true;
    }

    public User getUser() { return user; }

    public Role getRole() {
        return role;
    }
}
