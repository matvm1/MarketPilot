package com.marketpilot.application.services;

import com.marketpilot.domain.entities.auth.Role;
import integration.BaseFixtureIT;
import objects.TestAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationServiceIT extends BaseFixtureIT {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private RegistrationService registrationService;

    @Test
    public void initiateClientRegistration_returnsPendingVerification_ForNewClient() {
        RegistrationService.RegistrationStatus result = registrationService.initiateClientRegistration("quinnjnew", TestAuthProperties.dummyPassword(),
                new Role.RoleName[]{Role.RoleName.PersonalInvestor}, "quinnjordan_new@personal.com", "Quinn", "A", "Jordan");
        assertEquals(RegistrationService.RegistrationStatus.PENDING_VERIFICATION, result);
    }

    @Test
    public void initiateEmployeeRegistration_returnsPendingVerification_ForNewEmployee() {
        RegistrationService.RegistrationStatus result = registrationService.initiateEmployeeRegistration("ab123457", "quinnjnew",
                TestAuthProperties.dummyPassword(),
                new Role.RoleName[]{Role.RoleName.PersonalInvestor}, "quinnjordan_new@company.com", "Quinn", "A", "Jordan");
        assertEquals(RegistrationService.RegistrationStatus.PENDING_VERIFICATION, result);
    }
}
