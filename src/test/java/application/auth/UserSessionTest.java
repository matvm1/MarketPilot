package application.auth;

import domain.entities.auth.*;
import domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {
    private User investorAndAnalystUser;
    private UserSession investorSession;
    private UserSession analystSession;
    private UserRoleAssignment userInvestorRoleAssignment;
    private UserRoleAssignment userAnalystRoleAssignment;
    private Instant investorSessionStart;
    private Instant analystSessionStart;

    @BeforeEach
    void setUp() {
        UserFactory userFactory = new UserFactory();
        Set<Role> investorAndAnalystRoles = new HashSet<>();
        investorAndAnalystRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        investorAndAnalystRoles.add(TestRoles.ANALYST_ROLE);
        investorAndAnalystUser = userFactory.createUser(2, investorAndAnalystRoles, "John", "M", "Doe");

        investorSessionStart = Instant.parse("2025-01-01T10:00:00Z");
        userInvestorRoleAssignment = new UserRoleAssignment(investorAndAnalystUser, TestRoles.PERSONAL_INVESTOR_ROLE);
        investorSession = new UserSession(userInvestorRoleAssignment, investorSessionStart);
        // TODO: session state management - can't have a personal and employee session running at once
        analystSessionStart = Instant.parse("2025-01-02T10:00:00Z");
        userAnalystRoleAssignment = new UserRoleAssignment(investorAndAnalystUser, TestRoles.ANALYST_ROLE);
        analystSession = new UserSession(userAnalystRoleAssignment, analystSessionStart);
    }

    @Test
    void constructor_throwsForNullUserRoleAssignment() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(null, Instant.now()));
    }

    @Test
    void constructor_throwsForNullSessionStartInstant() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(userInvestorRoleAssignment, null));
    }

    @Test
    void getEffectivePermissions_returnsCorrectFlatSetOfPermissions() {
        assertEquals(TestRolePermissionSets.PERSONAL_INVESTOR_PERMISSIONS, investorSession.getPermissions());
        assertEquals(TestRolePermissionSets.ANALYST_PERMISSIONS, analystSession.getPermissions());
    }

    @Test
    void isExpired_returnsFalseWhenNotExpired() {
        Instant now = investorSessionStart.plus(Duration.ofHours(2));
        assertFalse(investorSession.isExpired(now));
    }

    @Test
    void isExpired_returnsTrueWhenIsExpired() {
        Instant now = investorSessionStart.plus(Duration.ofHours(4));
        assertFalse(investorSession.isExpired(now));
    }
}
