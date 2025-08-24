package domain.entities.auth;

public class UserRoleAsset {
    private Role role;
    private boolean isActive;

    public UserRoleAsset(Role role) {
        if (role == null)
            throw new IllegalArgumentException("role cannot be null");
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

    public Role getRole() {
        return role;
    }
}
