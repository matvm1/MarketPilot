package com.marketpilot.application.services;

import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.domain.entities.auth.Role;
import integration.BaseRegistrationIT;
import objects.TestAuthProperties;
import objects.TestRoles;
import com.marketpilot.util.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationServiceIT extends BaseRegistrationIT {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private AuthenticationService authenticationService;

    private static final String clientEmail = "quinnjordan@personal.com";
    private static final String employeeEmail = "quinnjordan@company.com";

    @Test
    public void initiateClientAuthentication_succeedsForValidCredentials() {
        Tuple<AuthenticationService.AuthenticationStatus, Optional<AuthenticationContext>> result =
                authenticationService.initiateClientAuthentication(clientEmail, TestAuthProperties.dummyPasswordHash(), Role.RoleName.PersonalInvestor);
        assertEquals(AuthenticationService.AuthenticationStatus.AWAITING_2FA, result.t());
        assertTrue(result.u().isPresent());
        assertEquals(clientUser, result.u().get().principal());
        assertEquals(TestRoles.personalInvestorRole(), result.u().get().role());
        //jdbc
    }
}
