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

    public Role(RoleName roleName, Set<Permission> permissions) {
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");
        if (permissions == null)
            throw new IllegalArgumentException("permissions cannot be null");
        if (permissions.isEmpty())
            throw new IllegalArgumentException("permissions cannot be empty");

        this.roleName = roleName;
        this.permissions = new HashSet<>(permissions);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
