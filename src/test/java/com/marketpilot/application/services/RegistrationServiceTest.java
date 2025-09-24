package com.marketpilot.application.services;

import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.persistence.EmployeeRepository;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.marketpilot.application.services.RegistrationService.RegistrationStatus;

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
    @Mock private EmployeeRepository employeeRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailEngine emailEngine;
    @Mock private PasswordHasher passwordHasher;

    private RegistrationService registrationService;

    private User existingClient;
    private User existingEmployee;
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
                employeeRepository, roleRepository, emailEngine, passwordHasher, userFactory);

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

        lenient().when(employeeRepository.employeeIdExists("ab123456")).thenReturn(true);
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe1",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsFailureIfUserNameOrPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        String nonRegisteredPasswordHash = "12345678";
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistration("johnmdoe",
                       nonRegisteredPasswordHash, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistration("johnmdoe1",
                        nonRegisteredPasswordHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateClientRegistration("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertDoesNotThrow(() -> registrationService.initiateClientRegistration("johnmdoe",
                dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateClientRegistration("johnmdoe", dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com",
                        "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfUserNameOrPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        String nonRegisteredPasswordHash = "12345678";
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        nonRegisteredPasswordHash, clientRoleNames, "johnmdoe1@outlook.com"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe1",
                        nonRegisteredPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsPendingVerificationWhenUserIsRegisteredAsClientAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1", dummyPasswordHash,
                        employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(employeeRepository.employeeIdExists("ab987654")).thenReturn(true);
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe",
                        dummyPasswordHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeEmailIsTaken() {
        when(employeeRepository.employeeIdExists("ab987654")).thenReturn(true);
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe1",
                        dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfUserNameOrEmployeeIdOrEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        String nonRegisteredPasswordHash = "12345678";
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfEmployeeIdNotFound() {
        when(employeeRepository.employeeIdExists("ab123456")).thenReturn(false);
        assertEquals(RegistrationStatus.FAILURE, registrationService.initiateEmployeeRegistration("ab123456",
                "johnmdoe", dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com",
                "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@company.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        assertDoesNotThrow(() -> registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe",
                        dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsAlreadyRegisteredIfEmployeeEmailIsRegistered() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUserNameOrEmployeeIdOrEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        String nonRegisteredPasswordHash = "12345678";
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123457", "johnmdoe",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com"));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123457", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfEmployeeIdNotFound() {
        when(employeeRepository.employeeIdExists("ab123456")).thenReturn(false);
        assertEquals(RegistrationStatus.FAILURE, registrationService.initiateEmployeeRegistrationForExistingClient("ab123456",
                "johnmdoe", dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsPendingVerificationIfUserIsRegisteredAsEmployeeAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@company.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPasswordHash, employeeRoleNames, "johnmdoe@company.com"));
    }

    @Test
    void completeRegistration_returnsFailureIfUserTypeIsNull() {
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                null, "123456"));
    }

    @Test
    void completeRegistration_returnsFailureIfUserIsNotPendingVerification(){
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                UserType.CLIENT, "123456"));
    }

    @Test
    void completeRegistration_returnsFailureIfClientVerificationCodeDoesNotMatch() {
        when(pendingVerificationUserRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(pendingVerificationUserRepository.getClientRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                UserType.CLIENT, "123457"));
    }

    @Test
    void completeRegistration_returnsFailureIfEmployeeVerificationCodeDoesNotMatch() {
        when(pendingVerificationUserRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(pendingVerificationUserRepository.getEmployeeRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                UserType.EMPLOYEE, "123457"));
    }

    @Test
    void completeRegistration_returnsSuccessIfClientVerificationCodeMatches() {
        when(pendingVerificationUserRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(pendingVerificationUserRepository.getClientRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        when(pendingVerificationUserRepository.deleteByUUID(any(UUID.class))).thenReturn(true);
        when(userRepository.save(existingClient)).thenReturn(true);
        assertEquals(RegistrationStatus.SUCCESS, registrationService.completeRegistration("johnmdoe",
                UserType.CLIENT, "123456"));
    }

    @Test
    void completeRegistration_returnsSuccessIfEmployeeVerificationCodeMatches() {
        when(pendingVerificationUserRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(existingClient));
        when(pendingVerificationUserRepository.getEmployeeRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        when(pendingVerificationUserRepository.deleteByUUID(any(UUID.class))).thenReturn(true);
        when(userRepository.save(existingClient)).thenReturn(true);
        assertEquals(RegistrationStatus.SUCCESS, registrationService.completeRegistration("johnmdoe",
                UserType.EMPLOYEE, "123456"));
    }
}
