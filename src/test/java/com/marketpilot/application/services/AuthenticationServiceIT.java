package com.marketpilot.application.services;

import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.domain.entities.auth.Role;
import integration.RegistrationFixtureIT;
import objects.TestAuthProperties;
import objects.TestRoles;
import com.marketpilot.util.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationServiceIT extends RegistrationFixtureIT {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private AuthenticationService authenticationService;

    private static final String clientEmail = "quinnjordan@personal.com";
    private static final String employeeId = "ab123456";
    private static final String employeeEmail = "quinnjordan@company.com";

    @Test
    public void initiateClientAuthentication_succeedsForValidCredentials() {
        Tuple<AuthenticationService.AuthenticationStatus, Optional<AuthenticationContext>> result =
                authenticationService.initiateClientAuthentication(clientEmail, TestAuthProperties.dummyPassword(), Role.RoleName.PersonalInvestor);
        assertEquals(AuthenticationService.AuthenticationStatus.AWAITING_2FA, result.t());
        assertTrue(result.u().isPresent());
        assertEquals(clientUser, result.u().get().principal());
        assertEquals(TestRoles.personalInvestorRole(), result.u().get().role());
    }

    @Test
    public void initiateEmployeeAuthentication_succeedsForValidCredentials() {
        Tuple<AuthenticationService.AuthenticationStatus, Optional<AuthenticationContext>> result =
                authenticationService.initiateEmployeeAuthentication(employeeId, TestAuthProperties.dummyPassword(), Role.RoleName.Analyst);
        assertEquals(AuthenticationService.AuthenticationStatus.AWAITING_2FA, result.t());
        assertTrue(result.u().isPresent());
        assertEquals(employeeUser, result.u().get().principal());
        assertEquals(TestRoles.analystRole(), result.u().get().role());
    }
}
