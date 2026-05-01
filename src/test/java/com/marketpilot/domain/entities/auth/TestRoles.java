package com.marketpilot.domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public final class TestRoles {

    private TestRoles() {}

    public static Role publicUser() {
        return new Role(Role.RoleName.Public, TestRolePermissionSets.authenticatedBasePermissions(), UserType.CLIENT);
    }

    public static Role personalInvestorRole() {
        return new Role(Role.RoleName.PersonalInvestor, TestRolePermissionSets.personalInvestorPermissions(), UserType.CLIENT);
    }

    public static Role analystRole() {
        return new Role(Role.RoleName.Analyst, TestRolePermissionSets.analystPermissions(), UserType.EMPLOYEE);
    }

    public static Role adminRole() {
        // TODO: Mock Admin permissions per business rules
        return new Role(Role.RoleName.Admin, allPermissions(), UserType.EMPLOYEE);
    }

    public static Set<Permission> allPermissions() {
        Set<Permission> permissions = new HashSet<>();
        permissions.addAll(TestRolePermissionSets.personalInvestorPermissions());
        permissions.addAll(TestRolePermissionSets.analystPermissions());
        return permissions;
    }

    public static Set<Role> all() {
        return new HashSet<>(Set.of(publicUser(), personalInvestorRole(), adminRole(), analystRole()));
    }

    public static Set<Role> allClient() {
        return new HashSet<>(Set.of(publicUser(), personalInvestorRole()));
    }

    public static Set<Role> allEmployee() {
        return new HashSet<>(Set.of(adminRole(), analystRole()));
    }
}