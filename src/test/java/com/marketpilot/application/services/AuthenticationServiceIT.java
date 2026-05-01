package com.marketpilot.application.services;

import com.marketpilot.MarketPilotApplication;
import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.repo.AuthRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ContextConfiguration(classes = MarketPilotApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationServiceIT {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private AuthenticationService authenticationService;

    private final String clientEmail = "clientEmail@provider.com";
    private final String employeeEmail = "employeeEmail@provider.com";
    private final byte[] dummyPasswordHash = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");

    @Test
    public void initiateClientAuthentication_succeedsForValidCredentials() {
        Tuple<AuthenticationService.AuthenticationStatus, Optional<AuthenticationContext>> result =
                authenticationService.initiateClientAuthentication(clientEmail, dummyPasswordHash, Role.RoleName.PersonalInvestor);
        assertEquals(AuthenticationService.AuthenticationStatus.AWAITING_2FA, result.t());
        assertTrue(result.u().isPresent());
        assertEquals(expectedClientUser, result.u().get().principal());
        assertEquals(TestRoles.personalInvestorRole(), result.u().get().role());
        //jdbc
    }
}
