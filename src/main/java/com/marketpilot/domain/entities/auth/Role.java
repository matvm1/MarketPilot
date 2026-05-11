package com.marketpilot.domain.entities.auth;

import com.marketpilot.domain.entities.PersistentEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "APP_ROLE")
public class Role extends PersistentEntity {
    public enum RoleName {
        Admin,
        PersonalInvestor,
        Analyst,
        Public;

        public String displayName() {
            return switch (this) {
                case Admin -> "Admin";
                case PersonalInvestor -> "Personal Investor";
                case Analyst -> "Analyst";
                case Public -> "Public";
                default -> name();
            };
        }
    }

    public Role() {}

    @Enumerated(EnumType.ORDINAL) private RoleName roleName;
    @Enumerated(EnumType.ORDINAL) private UserType userType;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "APP_ROLE_PERMISSION",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Enumerated(EnumType.ORDINAL) private Set<Permission> permissions;

    public Role(RoleName roleName, Set<Permission> permissions, UserType userType) {
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");
        if (permissions == null)
            throw new IllegalArgumentException("permissions cannot be null");
        if (permissions.isEmpty())
            throw new IllegalArgumentException("permissions cannot be empty");
        if (userType == null)
            throw new IllegalArgumentException("userType cannot be null");

        this.roleName = roleName;
        this.permissions = new HashSet<>(permissions);
        this.userType = userType;
    }

    public RoleName getRoleName() { return roleName; }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public UserType getUserType() { return userType; }

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
