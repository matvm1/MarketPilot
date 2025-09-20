package com.marketpilot.domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public class Role {
    public enum RoleName {
        Admin,
        PersonalInvestor,
        Analyst,
        Public
    }

    public enum RoleType {
        CLIENT,
        EMPLOYEE
    }

    private final RoleName roleName;
    private final Set<Permission> permissions;
    private final RoleType roleType;

    public Role(RoleName roleName, Set<Permission> permissions, RoleType roleType) {
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");
        if (permissions == null)
            throw new IllegalArgumentException("permissions cannot be null");
        if (permissions.isEmpty())
            throw new IllegalArgumentException("permissions cannot be empty");

        this.roleName = roleName;
        this.permissions = new HashSet<>(permissions);
        this.roleType = roleType;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public RoleType getRoleType() { return roleType; }

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
