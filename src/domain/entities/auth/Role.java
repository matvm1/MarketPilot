package domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public class Role {
    private enum RoleName {
        Admin,
        PersonalInvestor,
        Analyst,
        Public
    }

    private RoleName roleName;
    private Set<Permission> permissions;

    public Role(RoleName roleName, Set<Permission> permissions) {
        this.roleName = roleName;
        this.permissions = new HashSet<>(permissions);
    }
}
