package com.marketpilot.application.services;

import com.marketpilot.domain.entities.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserFactoryTest {
    private UserFactory userFactory;
    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;
    private Set<Role> investorAndAnalystRoles;
    private User clientUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);
        investorAndAnalystRoles = new HashSet<>();
        investorAndAnalystRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        investorAndAnalystRoles.add(TestRoles.ANALYST_ROLE);

        clientUser = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        employeeUser = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe",
                "johnmdoe@company.com", "John", "M", "Doe");
    }

    @Test
    void createClientUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        assertNotEquals(null, user.getRoles());
        assertFalse(user.getRoles().isEmpty());
    }

    @Test
    void createClientUser_returnsUserWithUUID() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        assertDoesNotThrow(user::getUUID);
        assertNotEquals(null, user.getUUID());
    }

    @Test
    void createEmployeeUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");
        assertNotEquals(null, user.getRoles());
        assertFalse(user.getRoles().isEmpty());
    }

    @Test
    void createEmployeeUser_returnsUserWithUUID() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");
        assertDoesNotThrow(user::getUUID);
        assertNotEquals(null, user.getUUID());
    }

    @Test
    void assignEmployeeAttributes_throwsIfExistingClientIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(null, "ab123456", employeeRoles, "johnmdoe@cmopany.com"));
    }

    @Test
    void assignEmployeeAttributes_throwsIfEmployeeRolesIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(clientUser, "ab123456", null, "johnmdoe@company.com"));
    }

    @Test
    void assignEmployeeAttributes_throwsIfEmployeeRolesIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(clientUser, "ab123456", new HashSet<>(), "johnmdoe@company.com"));
    }

    @Test
    void assignClientAttributes_throwsIfExistingEmployeeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(null, clientRoles, "johnmdoe@cmopany.com"));
    }

    @Test
    void assignClientAttributes_throwsIfClientRolesIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(employeeUser, null, "johnmdoe@company.com"));
    }

    @Test
    void assignClientAttributes_throwsIfClientRolesIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(employeeUser, new HashSet<>(), "johnmdoe@company.com"));
    }

    @Test
    void validateRolesAndGrant_throwsIfRoleHasUnexpectedUserType() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.validateRolesAndGrant(clientUser, employeeRoles, UserType.CLIENT));
    }

    @Test
    void validateRolesAndGrant_grantsRolesToUserIfValidationSuccessful() {
        assertDoesNotThrow(() ->
                userFactory.validateRolesAndGrant(clientUser, employeeRoles, UserType.EMPLOYEE));

        Set<Role> expectedRoles = new HashSet<>(clientRoles);
        expectedRoles.addAll(employeeRoles);

        assertEquals(expectedRoles, clientUser.getRoles());
    }
}
