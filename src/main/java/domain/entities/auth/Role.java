package domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public class Role {
    public enum RoleName {
        Admin,
        PersonalInvestor,
        Analyst,
        Public
    }

    private RoleName roleName;
    private Set<Permission> permissions;
    private boolean isActive;

    public Role(RoleName roleName, Set<Permission> permissions) {
        this.roleName = roleName;
        this.permissions = new HashSet<>(permissions);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
