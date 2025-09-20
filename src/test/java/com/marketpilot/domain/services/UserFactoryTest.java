package com.marketpilot.domain.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserRoleAssignment;
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

    private static final String BCRYPT_STRONG_PASSWORD = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        investorAndAnalystRoles = new HashSet<>();
        clientRoles = new HashSet<>();
        employeeRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles.add(TestRoles.ANALYST_ROLE);
        investorAndAnalystRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        investorAndAnalystRoles.add(TestRoles.ANALYST_ROLE);
    }

    @Test
    void createClientUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD,
                "johnmdoe@outlook.com","John", "M", "Doe");
        assertNotEquals(null, user.getUserRoleAssignments());
        assertFalse(user.getUserRoleAssignments().isEmpty());
    }

    @Test
    void createClientUser_userRoleAssignmentsReferBackToUser() {
        User user = userFactory.createClientUser(clientRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD,
                "johnmdoe@outlook.com","John", "M", "Doe");
        for (UserRoleAssignment userRoleAssignment : user.getUserRoleAssignments())
            assertEquals(userRoleAssignment.getUser(), user);
    }

    @Test
    void createEmployeeUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD,
                "johnmdoe@company.com","John", "M", "Doe");
        assertNotEquals(null, user.getUserRoleAssignments());
        assertFalse(user.getUserRoleAssignments().isEmpty());
    }

    @Test
    void createEmployeeUser_userRoleAssignmentsReferBackToUser() {
        User user = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", BCRYPT_STRONG_PASSWORD,
                "johnmdoe@company.com","John", "M", "Doe");
        for (UserRoleAssignment userRoleAssignment : user.getUserRoleAssignments())
            assertEquals(userRoleAssignment.getUser(), user);
    }
}
