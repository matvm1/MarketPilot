package application.entities;

import domain.entities.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {
    private User analystAndInvestorUser;
    private UserSession investorSession;
    private UserSession analystSession;
    private Instant sessionStartTime;
    private UserRoleAssignment analystRoleAssignment;
    private UserRoleAssignment investorRoleAssignment;
    private Instant investorSessionStart;
    private Instant analystSessionStart;

    @BeforeEach
    void setUp() {
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        investorRoleAssignment = new UserRoleAssignment(new Role(Role.RoleName.PersonalInvestor,
                RolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS));
        analystRoleAssignment = new UserRoleAssignment(new Role(Role.RoleName.Analyst,
            RolePermissionSets.ANALYST_PERMISSIONS));
        userRoleAssignments.add(investorRoleAssignment);
        userRoleAssignments.add(analystRoleAssignment);
        analystAndInvestorUser = new User(2, userRoleAssignments, "John", "M", "Doe");
        investorSessionStart = Instant.parse("2025-01-01T10:00:00Z");
        investorSession = new UserSession(investorRoleAssignment, investorSessionStart);
        // TODO: session state management - can't have a personal and employee session running at once
        analystSessionStart = Instant.parse("2025-01-02T10:00:00Z");
        analystSession = new UserSession(analystRoleAssignment, analystSessionStart);
    }

    @Test
    void constructorThrowsForNullUserRoleAssignment() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(null, Instant.now()));
    }

    @Test
    void constructorThrowsForNullSessionStartInstant() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(investorRoleAssignment, null));
    }

    @Test
    void getEffectivePermissionsReturnsCorrectFlatSetOfPermissions() {
        assertEquals(RolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS, investorSession.getPermissions());
        assertEquals(RolePermissionSets.ANALYST_PERMISSIONS, analystSession.getPermissions());
    }

    @Test
    void testSessionIsNotExpired() {
        Instant now = investorSessionStart.plus(Duration.ofHours(2));
        assertFalse(investorSession.isExpired(now));
    }

    @Test
    void testSessionIsExpired() {
        Instant now = investorSessionStart.plus(Duration.ofHours(4));
        assertFalse(investorSession.isExpired(now));
    }
}
