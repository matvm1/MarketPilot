package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.marketpilot.application.services.RegistrationService.RegistrationResult;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    UserFactory userFactory;
    @Mock private UserRepository userRepository;
    @Mock private PendingVerificationUserRepository pendingVerificationUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TwoFactorService twoFactorService;
    @Mock private PasswordHasher passwordHasher;

    private RegistrationService registrationService;

    private User johnMDoe;
    private char[] dummyPassword;
    private static final String dummyPasswordHash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private Set<RoleName> clientRoleNames;
    private Set<RoleName> employeeRoleNames;

    @BeforeEach
    void setUp() {
        johnMDoe = new User("ab123456", "johnmdoe", "johnmdoe@outlook.com", "johnmdoe@company.com", "John", "M", "Doe");
        dummyPassword = new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        userFactory = new UserFactory();
        registrationService = new RegistrationService(userRepository, pendingVerificationUserRepository,
                roleRepository, twoFactorService, passwordHasher, userFactory);

        clientRoleNames = new HashSet<>();
        clientRoleNames.add(RoleName.PersonalInvestor);
        employeeRoleNames = new LinkedHashSet<>();
        employeeRoleNames.add(RoleName.Analyst);
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(johnMDoe));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe1",
                       dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(johnMDoe));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1", dummyPassword,
                        employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeEmailIsTaken() {
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(johnMDoe));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe1",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateClientRegistration("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        assertDoesNotThrow(() -> registrationService.initiateClientRegistration("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                registrationService.initiateClientRegistration("johnmdoe", dummyPassword, clientRoleNames, "johnmdoe@outlook.com",
                        "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        assertDoesNotThrow(() -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                 dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                assertDoesNotThrow(() -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe")));
        }
}
