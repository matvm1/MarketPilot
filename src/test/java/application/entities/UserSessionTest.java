package application.entities;

import domain.entities.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserSessionTest {
    private User analystAndInvestorUser;
    private UserSession investorSession;
    private UserSession analystSession;

    @BeforeEach
    void setUp() {
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        userRoleAssignments.add(new UserRoleAssignment(new Role(Role.RoleName.PersonalInvestor, RolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS)));
        userRoleAssignments.add(new UserRoleAssignment(new Role(Role.RoleName.Analyst, RolePermissionSets.ANALYST_PERMISSIONS)));
        analystAndInvestorUser = new User(2, userRoleAssignments, "John", "M", "Doe");
        investorSession = new UserSession(analystAndInvestorUser, Role.RoleName.PersonalInvestor);
        // TODO: session state management - can't have a personal and employee session running at once
        investorSession = new UserSession(analystAndInvestorUser, Role.RoleName.Analyst);
    }

    @Test
    void constructorThrowsForNullUser() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(null, Role.RoleName.PersonalInvestor));
    }

    @Test
    void constructorThrowsForNullActiveRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(analystAndInvestorUser, null));
    }

    @Test
    void getEffectivePermissionsReturnsCorrectFlatSetOfPermissions() {
        assertEquals(RolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS, investorSession.getEffectivePermissions());
        assertEquals(RolePermissionSets.ANALYST_PERMISSIONS, analystSession.getEffectivePermissions());
    }
}
