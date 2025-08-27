package domain.services;

import domain.entities.auth.*;
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
        User user = userFactory.createUser(1, investorAndAnalystRoles, "John", "M", "Doe");
        assertNotEquals(null, user.getUserRoleAssignments());
        assertFalse(user.getUserRoleAssignments().isEmpty());
    }

    @Test
    void createUsr_userRoleAssignmentsReferBackToUser() {
        User user = userFactory.createUser(1, investorAndAnalystRoles, "John", "M", "Doe");
        for (UserRoleAssignment userRoleAssignment : user.getUserRoleAssignments())
            assertEquals(userRoleAssignment.getUser(), user);
    }
}
