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

    private final RoleName roleName;
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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        if (permission == null)
            return false;

        return permissions.contains(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Role))
            return false;

        return ((Role)o).permissions.equals(permissions) && ((Role)o).roleName == roleName;
    }
}
