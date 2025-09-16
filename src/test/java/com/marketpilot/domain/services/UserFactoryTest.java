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
    private Set<Role> investorAndAnalystRoles;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        investorAndAnalystRoles = new HashSet<>();
        investorAndAnalystRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        investorAndAnalystRoles.add(TestRoles.ANALYST_ROLE);
    }

    @Test
    void createUser_returnsUserWithNonNullAndNonEmptyRoleAssignments() {
        User user = userFactory.createUser("ab123456", investorAndAnalystRoles, "johnmdoe", "johnmdoe@outlook.com",
                "johnmdoe@company.com","John", "M", "Doe");
        assertNotEquals(null, user.getUserRoleAssignments());
        assertFalse(user.getUserRoleAssignments().isEmpty());
    }

    @Test
    void createUsr_userRoleAssignmentsReferBackToUser() {
        User user = userFactory.createUser("ab123456", investorAndAnalystRoles, "johnmdoe", "johnmdoe@outlook.com",
                "johnmdoe@company.com","John", "M", "Doe");
        for (UserRoleAssignment userRoleAssignment : user.getUserRoleAssignments())
            assertEquals(userRoleAssignment.getUser(), user);
    }
}
