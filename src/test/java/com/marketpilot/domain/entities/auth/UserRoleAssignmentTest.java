package com.marketpilot.domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleAssignmentTest {
    private Role dummyRole;
    UserRoleAssignment dummyUserRoleAssignment;

    @BeforeEach
    void setUp() {
        dummyRole = TestRoles.PERSONAL_INVESTOR_ROLE;
        dummyUserRoleAssignment = new UserRoleAssignment(new User("ab123456", "johnmdoe", "johnmdoe@outlook.com",
                "johnmdoe@company.com","John", "M", "Doe"), dummyRole);
    }

    @Test
    void constructor_throwsForNullUser() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserRoleAssignment(null, dummyRole));
    }

    @Test
    void constructor_throwsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserRoleAssignment(new User("ab123456", "johnmdoe", "johnmdoe@outlook.com",
                        "johnmdoe@company.com","John", "M", "Doe"), null));
    }

    @Test
    void constructor_initIsActiveToFalse() {
        assertFalse(dummyUserRoleAssignment.isActive());
    }

    @Test
    void isActive_returnsTrueWhenActive() {
        dummyUserRoleAssignment.setActive();
        assertTrue(dummyUserRoleAssignment.isActive());
    }

    @Test
    void isActive_returnsFalseWhenInactive() {
        dummyUserRoleAssignment.setInactive();
        assertFalse(dummyUserRoleAssignment.isActive());
    }

    @Test
    void setActive_returnsTrueWhenPreviouslyInactive() {
        assertTrue(dummyUserRoleAssignment.setActive());
    }

    @Test
    void setActive_returnsFalseWhenPreviouslyActive() {
        dummyUserRoleAssignment.setActive();
        assertFalse(dummyUserRoleAssignment.setActive());
    }

    @Test
    void setInactive_returnsTrueWhenPreviouslyActive() {
        dummyUserRoleAssignment.setActive();
        assertTrue(dummyUserRoleAssignment.setInactive());
    }

    @Test
    void setInactive_returnsFalseWhenPreviouslyInactive() {
        assertFalse(dummyUserRoleAssignment.setInactive());
    }
}
