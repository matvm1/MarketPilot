package com.marketpilot.application.services;

import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.domain.entities.auth.*;
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
    private Instant investorSessionStart;
    private Instant analystSessionStart;

    // TODO: Register test user as a personal investor first, then as an analyst employee
    @BeforeEach
    void setUp() {
        UserFactory userFactory = new UserFactory();
        Set<Role> employeeRoles = new HashSet<>();
        //employeeRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles.add(TestRoles.analystRole());
        investorAndAnalystUser = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe","johnmdoe@company.com",
                "John", "M", "Doe");

        investorSessionStart = Instant.parse("2025-01-01T10:00:00Z");
        investorSession = new UserSession(1, new AuthenticationContext(investorAndAnalystUser, TestRoles.personalInvestorRole()), investorSessionStart);
        // TODO: session state management - can't have a personal and employee session running at once
        analystSessionStart = Instant.parse("2025-01-02T10:00:00Z");
        analystSession = new UserSession(2, new AuthenticationContext(investorAndAnalystUser, TestRoles.analystRole()), analystSessionStart);
    }

    //TODO: consider safer data type for ids (UUID?)
    @Test
    void constructor_throwsForNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(0, new AuthenticationContext(investorAndAnalystUser, TestRoles.personalInvestorRole()), Instant.now()));
    }

    @Test
    void constructor_throwsForNullAuth() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(1, null, Instant.now()));
    }

    @Test
    void constructor_throwsForNullSessionStartInstant() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserSession(1, new AuthenticationContext(investorAndAnalystUser, TestRoles.personalInvestorRole()), null));
    }

    @Test
    void getEffectivePermissions_returnsCorrectFlatSetOfPermissions() {
        assertEquals(TestRolePermissionSets.personalInvestorPermissions(), investorSession.getPermissions());
        assertEquals(TestRolePermissionSets.analystPermissions(), analystSession.getPermissions());
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
