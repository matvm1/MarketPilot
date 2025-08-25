package domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleAssignmentTest {
    private Role dummyRole;
    UserRoleAssignment dummyUserRoleAssignment;

    @BeforeEach
    void setUp() {
        Set<Permission> dummyPermissions = new HashSet<>();
        dummyPermissions.add(Permission.PLACE_TRADE);
        dummyRole = new Role(Role.RoleName.PersonalInvestor, dummyPermissions);
        dummyUserRoleAssignment = new UserRoleAssignment(dummyRole);
    }

    @Test
    void constructorThrowsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserRoleAssignment(null));
    }

    @Test
    void constructorInitIsActiveToFalse() {
        assertFalse(dummyUserRoleAssignment.isActive());
    }

    @Test
    void isActiveReturnsTrueWhenActive() {
        dummyUserRoleAssignment.setActive();
        assertTrue(dummyUserRoleAssignment.isActive());
    }

    @Test
    void isActiveReturnsFalseWhenInactive() {
        dummyUserRoleAssignment.setInactive();
        assertFalse(dummyUserRoleAssignment.isActive());
    }

    @Test
    void setActiveReturnsTrueWhenPreviouslyInactive() {
        assertTrue(dummyUserRoleAssignment.setActive());
    }

    @Test
    void setActiveReturnsFalseWhenPreviouslyActive() {
        dummyUserRoleAssignment.setActive();
        assertFalse(dummyUserRoleAssignment.setActive());
    }

    @Test
    void setInactiveReturnsTrueWhenPreviouslyActive() {
        dummyUserRoleAssignment.setActive();
        assertTrue(dummyUserRoleAssignment.setInactive());
    }

    @Test
    void setInactiveReturnsFalseWhenPreviouslyInactive() {
        assertFalse(dummyUserRoleAssignment.setInactive());
    }
}
