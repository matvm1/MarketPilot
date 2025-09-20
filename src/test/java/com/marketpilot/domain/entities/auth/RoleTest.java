package com.marketpilot.domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {
    private Set<Permission> dummyPermissions;
    private Role dummyRole;

    @BeforeEach
    void setUp() {
        dummyPermissions = new HashSet<>();
        dummyPermissions.add(Permission.CREATE_USER);
        dummyPermissions.add(Permission.DELETE_USER);
        dummyRole = new Role(Role.RoleName.PersonalInvestor, dummyPermissions, Role.RoleType.CLIENT);
    }

    @Test
    void constructorThrowsForNullRoleName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(null, dummyPermissions, Role.RoleType.CLIENT));
    }

    @Test
    void constructorThrowsForNullPermissions() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(Role.RoleName.Public, null, Role.RoleType.CLIENT));
    }

    @Test
    void constructorThrowsForEmptyPermissions() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(Role.RoleName.Public, new HashSet<>(), Role.RoleType.CLIENT));
    }

    @Test
    void getPermissionsIsNotNull() {
        assertNotEquals(null, dummyRole.getPermissions());
    }

    @Test
    void hasPermissionReturnsTrueWhenRoleHasPermission() {
        assertTrue(dummyRole.hasPermission(Permission.CREATE_USER));
    }

    @Test
    void hasPermissionReturnsFalseWhenRolesDoesNotHavePermission() {
        assertFalse(dummyRole.hasPermission(Permission.PUBLISH_ARTICLE));
    }

    @Test
    void hasPermissionReturnsFalseWhenPermissionIsNull() {
        assertFalse(dummyRole.hasPermission(null));
    }

    @Test
    void equalsReturnsTrueWhenTwoRolesAreEqual() {
        Set<Permission> perms1 = new HashSet<>();
        perms1.add(Permission.CREATE_USER);
        perms1.add(Permission.DELETE_USER);
        Role r1 = new Role(Role.RoleName.Admin, perms1, Role.RoleType.EMPLOYEE);

        Set<Permission> perms2 = new HashSet<>();
        perms2.add(Permission.CREATE_USER);
        perms2.add(Permission.DELETE_USER);
        Role r2 = new Role(Role.RoleName.Admin, perms2, Role.RoleType.EMPLOYEE);

        assertEquals(r1, r2);
    }

    @Test
    void equalsReturnsFalseWhenTwoRolesAreNotEqual() {
        Set<Permission> perms1 = new HashSet<>();
        perms1.add(Permission.CREATE_WATCHLIST);
        perms1.add(Permission.PUBLISH_ARTICLE);
        Role r1 = new Role(Role.RoleName.Analyst, perms1, Role.RoleType.EMPLOYEE);

        Set<Permission> perms2 = new HashSet<>();
        perms2.add(Permission.CREATE_USER);
        perms2.add(Permission.DELETE_USER);
        Role r2 = new Role(Role.RoleName.Admin, perms2, Role.RoleType.EMPLOYEE);

        assertNotEquals(r1, r2);
    }
}
