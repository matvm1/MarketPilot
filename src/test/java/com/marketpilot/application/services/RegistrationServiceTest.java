package com.marketpilot.application.services;

import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    UserFactory userFactory;
    @Mock private UserRepository userRepository;
    @Mock private PendingVerificationUserRepository pendingVerificationUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailEngine emailEngine;
    @Mock private PasswordHasher passwordHasher;

    private RegistrationService registrationService;

    private User existingClient;
    private User existingEmployee;
    private char[] dummyPassword = new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
    private static final String dummyPasswordHash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private Set<RoleName> clientRoleNames;
    private Set<RoleName> employeeRoleNames;
    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;
    private final String VERIFICATION_EMAIL_TEMPLATE = "verification_email.html";

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        registrationService = new RegistrationService(userRepository, pendingVerificationUserRepository,
                roleRepository, emailEngine, passwordHasher, userFactory);

        clientRoleNames = new HashSet<>();
        clientRoleNames.add(RoleName.PersonalInvestor);
        employeeRoleNames = new LinkedHashSet<>();
        employeeRoleNames.add(RoleName.Analyst);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);

        existingClient = userFactory.createClientUser(clientRoles, "johnmdoe", dummyPasswordHash,
                "johnmdoe@outlook.com", "John", "M", "Doe");
        existingEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", dummyPasswordHash,
                "johnmdoe@company.com", "John", "M", "Doe");

        lenient().when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe1",
                       dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsFailureIfUserNameOrPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        char[] nonRegisteredPassword = {'1', '2', '3'};
        when(passwordHasher.hash(nonRegisteredPassword)).thenReturn("hash123");
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateClientRegistration("johnmdoe",
                       nonRegisteredPassword, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateClientRegistration("johnmdoe1",
                        nonRegisteredPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateClientRegistration("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertDoesNotThrow(() -> registrationService.initiateClientRegistration("johnmdoe",
                dummyPassword, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                registrationService.initiateClientRegistration("johnmdoe", dummyPassword, clientRoleNames, "johnmdoe@outlook.com",
                        "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfUserNameOrPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        char[] nonRegisteredPassword = {'1', '2', '3'};
        when(passwordHasher.hash(nonRegisteredPassword)).thenReturn("hash123");
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        nonRegisteredPassword, clientRoleNames, "johnmdoe1@outlook.com"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe1",
                        nonRegisteredPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsPendingVerificationWhenUserIsRegisteredAsClientAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1", dummyPassword,
                        employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeEmailIsTaken() {
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe1",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfUserNameOrEmployeeIdOrEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        char[] nonRegisteredPassword = {'1', '2', '3'};
        when(passwordHasher.hash(nonRegisteredPassword)).thenReturn("hash123");
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe1",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@company.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        assertDoesNotThrow(() -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsAlreadyRegisteredIfEmployeeEmailIsRegistered() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationResult.ALREADY_REGISTERED,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPassword, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUserNameOrEmployeeIdOrEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        char[] nonRegisteredPassword = {'1', '2', '3'};
        when(passwordHasher.hash(nonRegisteredPassword)).thenReturn("hash123");
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe1",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe1@company.com"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123457", "johnmdoe",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe1@company.com"));
        assertEquals(RegistrationResult.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123457", "johnmdoe1",
                        nonRegisteredPassword, employeeRoleNames, "johnmdoe@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsPendingVerificationIfUserIsRegisteredAsEmployeeAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@company.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        assertEquals(RegistrationResult.PENDING_VERIFICATION,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com"));
    }
}
