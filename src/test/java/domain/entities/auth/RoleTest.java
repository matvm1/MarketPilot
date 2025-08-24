package domain.entities.auth;

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
        dummyRole = new Role(Role.RoleName.PersonalInvestor, dummyPermissions);
    }

    @Test
    void constructorThrowsForNullRoleName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(null, dummyPermissions));
    }

    @Test
    void constructorThrowsForNullPermissions() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(Role.RoleName.Public, null));
    }

    @Test
    void constructorThrowsForEmptyPermissions() {
        assertThrows(IllegalArgumentException.class, () ->
                new Role(Role.RoleName.Public, new HashSet<Permission>()));
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
}
