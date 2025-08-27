package domain.entities.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TestRoles {

    private TestRoles() {}

    public static final Role PUBLIC_USER = new Role(
            Role.RoleName.Public,
            TestRolePermissionSets.AUTHENTICATED_BASE_PERMISSIONS
    );

    public static final Role PERSONAL_INVESTOR_ROLE = new Role(
            Role.RoleName.PersonalInvestor,
            TestRolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS
    );

    public static final Role ANALYST_ROLE = new Role(
            Role.RoleName.Analyst,
            TestRolePermissionSets.ANALYST_PERMISSIONS
    );

    public static final Set<Permission> ALL_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>() {{
        addAll(TestRolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS);
        addAll(TestRolePermissionSets.ANALYST_PERMISSIONS);
    }});

    public static final Role ADMIN_ROLE = new Role(
            Role.RoleName.Admin,
            // TODO: Mock Admin permissions per business rules
            ALL_PERMISSIONS
    );
}