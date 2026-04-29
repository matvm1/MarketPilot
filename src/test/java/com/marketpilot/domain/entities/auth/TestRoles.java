package com.marketpilot.domain.entities.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TestRoles {

    private TestRoles() {}

    public static final Role PUBLIC_USER = new Role(
            Role.RoleName.Public,
            TestRolePermissionSets.authenticatedBasePermissions(),
            UserType.CLIENT
    );

    public static final Role PERSONAL_INVESTOR_ROLE = new Role(
            Role.RoleName.PersonalInvestor,
            TestRolePermissionSets.personalInvestorPermissions(),
            UserType.CLIENT
    );

    public static final Role ANALYST_ROLE = new Role(
            Role.RoleName.Analyst,
            TestRolePermissionSets.analystPermissions(),
            UserType.EMPLOYEE
    );

    public static final Set<Permission> ALL_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>() {{
        addAll(TestRolePermissionSets.personalInvestorPermissions());
        addAll(TestRolePermissionSets.analystPermissions());
    }});

    public static final Role ADMIN_ROLE = new Role(
            Role.RoleName.Admin,
            // TODO: Mock Admin permissions per business rules
            ALL_PERMISSIONS,
            UserType.EMPLOYEE
    );

    public static Set<Role> all() {
        Set<Role> roles = new HashSet<>();
        roles.add(PUBLIC_USER);
        roles.add(PERSONAL_INVESTOR_ROLE);
        roles.add(ADMIN_ROLE);
        roles.add(ANALYST_ROLE);

        return roles;
    }

    public static Set<Role> allClient() {
        Set<Role> roles = new HashSet<>();
        roles.add(PUBLIC_USER);
        roles.add(PERSONAL_INVESTOR_ROLE);

        return roles;
    }
    public static Set<Role> allEmployee() {
        Set<Role> roles = new HashSet<>();
        roles.add(ADMIN_ROLE);
        roles.add(ANALYST_ROLE);

        return roles;
    }
}