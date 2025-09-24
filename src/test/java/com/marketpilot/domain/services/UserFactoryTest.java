package com.marketpilot.domain.services;

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

    private static final String BCRYPT_STRONG_PASSWORD_CLIENT = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private static final String BCRYPT_STRONG_PASSWORD_EMPLOYEE = "$2a$12$vKx8mN2pQ7wE5rL9sA3bfOzT6yH4jC1nM8pR5sK2wE7qL9vX3mN8p";

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

        clientUser = userFactory.createClientUser(clientRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD_CLIENT,
                "johnmdoe@outlook.com", "John", "M", "Doe");
        employeeUser = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe",
                BCRYPT_STRONG_PASSWORD_EMPLOYEE, "johnmdoe@company.com", "John", "M", "Doe");
    }

    @Test
    void createClientUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD_CLIENT,
                "johnmdoe@outlook.com","John", "M", "Doe");
        assertNotEquals(null, user.getRoles());
        assertFalse(user.getRoles().isEmpty());
    }

    @Test
    void createClientUser_returnsUserWithUUID() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD_CLIENT,
                "johnmdoe@outlook.com","John", "M", "Doe");
        assertDoesNotThrow(user::getUUID);
        assertNotEquals(null, user.getUUID());
    }

    @Test
    void createEmployeeUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD_EMPLOYEE,
                "johnmdoe@company.com","John", "M", "Doe");
        assertNotEquals(null, user.getRoles());
        assertFalse(user.getRoles().isEmpty());
    }

    @Test
    void createEmployeeUser_returnsUserWithUUID() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD_EMPLOYEE,
                "johnmdoe@company.com","John", "M", "Doe");
        assertDoesNotThrow(user::getUUID);
        assertNotEquals(null, user.getUUID());
    }

    @Test
    void assignEmployeeAttributes_throwsIfExistingClientIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(null, "ab123456", employeeRoles,
                        BCRYPT_STRONG_PASSWORD_EMPLOYEE, "johnmdoe@cmopany.com"));
    }

    @Test
    void assignEmployeeAttributes_throwsIfEmployeeRolesIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(clientUser, "ab123456", null,
                        BCRYPT_STRONG_PASSWORD_EMPLOYEE, "johnmdoe@company.com"));
    }

    @Test
    void assignEmployeeAttributes_throwsIfEmployeeRolesIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignEmployeeAttributes(clientUser, "ab123456", new HashSet<>(),
                        BCRYPT_STRONG_PASSWORD_EMPLOYEE, "johnmdoe@company.com"));
    }

    @Test
    void assignClientAttributes_throwsIfExistingEmployeeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(null, clientRoles,
                        BCRYPT_STRONG_PASSWORD_CLIENT, "johnmdoe@cmopany.com"));
    }

    @Test
    void assignClientAttributes_throwsIfClientRolesIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(employeeUser, null,
                        BCRYPT_STRONG_PASSWORD_CLIENT, "johnmdoe@company.com"));
    }

    @Test
    void assignClientAttributes_throwsIfClientRolesIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                userFactory.assignClientAttributes(employeeUser, new HashSet<>(),
                        BCRYPT_STRONG_PASSWORD_CLIENT, "johnmdoe@company.com"));
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
