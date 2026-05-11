package com.marketpilot.application.services;

import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.domain.entities.auth.Role;
import objects.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import integration.BaseIT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationServiceIT extends BaseIT {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private AuthenticationService authenticationService;

    private static User expectedClient;
    private static User expectedEmployee;
    private static final String clientEmail = "clientEmail@provider.com";
    private static final String employeeEmail = "employeeEmail@provider.com";
    private static final byte[] dummyPasswordHash = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");

    private static Set<Role> clientRoles;
    private static Set<Role> employeeRoles;

    @BeforeAll
    public void setUp() {
        personalInvestorRole = TestRoles.personalInvestorRole();
        analystRole = TestRoles.analystRole();

        clientRoles = new HashSet<>();
        clientRoles.add(personalInvestorRole);

        employeeRoles = new HashSet<>();
        employeeRoles.add(analystRole);

        UserFactory userFactory = new UserFactory();
        expectedClient = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        expectedEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");
    }

    @Test
    public void initiateClientAuthentication_succeedsForValidCredentials() {
        Tuple<AuthenticationService.AuthenticationStatus, Optional<AuthenticationContext>> result =
                authenticationService.initiateClientAuthentication(clientEmail, dummyPasswordHash, Role.RoleName.PersonalInvestor);
        assertEquals(AuthenticationService.AuthenticationStatus.AWAITING_2FA, result.t());
        assertTrue(result.u().isPresent());
        assertEquals(expectedClient, result.u().get().principal());
        assertEquals(TestRoles.personalInvestorRole(), result.u().get().role());
        //jdbc
    }
}
