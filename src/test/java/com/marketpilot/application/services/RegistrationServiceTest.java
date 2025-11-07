package com.marketpilot.application.services;

import com.marketpilot.adapters.persistence.repo.RoleCache;
import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.domain.repo.EmployeeRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.marketpilot.application.services.RegistrationService.RegistrationStatus;

import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    private RoleCache roleCache;

    private RegistrationService registrationService;

    private User existingClient;
    private User existingEmployee;
    private RoleName[] clientRoleNames;
    private RoleName[] employeeRoleNames;
    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;
    private Set<Role> adminRoles;

    private final String CLIENT_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Account";
    private final String EMPLOYEE_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Employee Account";
    private final String VERIFICATION_EMAIL_TEMPLATE = "verification_email.html";

    // use dummyPasswordLightHash as argument to all authentication calls
    private byte[] dummyPasswordLightHash;
    private final byte[] dummySalt = BufferedConverter.toBytes("b234vhnosd9021bhj23vsdb#nkjb$bnjk32!mjkhn*msdhjb3493bn");
    private final byte[] dummyPasswordHash = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");
    private final byte[] dummyPasswordHashStored = dummyPasswordHash.clone();

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        Set<Role.RoleName> roleNameSet = Arrays.stream(Role.RoleName.values()).collect(Collectors.toSet());
        when(roleRepository.findByRoleNames(roleNameSet)).thenReturn(Optional.of(TestRoles.all()));
        roleCache = new RoleCache(roleRepository);
        roleCache.load();
        registrationService = new RegistrationService(userRepository, pendingVerificationUserRepository,
                employeeRepository, roleRepository, emailEngine, passwordHasher, userFactory, roleCache);

        clientRoleNames = new RoleName[] {RoleName.PersonalInvestor};
        employeeRoleNames = new RoleName[] {RoleName.Analyst};

        clientRoles = roleCache.fetch(clientRoleNames);
        employeeRoles = roleCache.fetch(employeeRoleNames);

        existingClient = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        existingEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");

        dummyPasswordLightHash = BufferedConverter.toBytes("nc36784gfyu43vbf7623frtycwdvtyuawjcevdfyu12b367821f");

        //TODO: avoid the use of lenient()
        Map<String, Object> existingClientProps = new HashMap<>();
        existingClientProps.put("EMPLOYEE_REGISTRATION_EXPIRATION", Instant.now().plus(Duration.ofMinutes(25)));
        lenient().when(pendingVerificationUserRepository.findByUsername(UserType.EMPLOYEE, existingClient.getUsername())).thenReturn(Optional.of(new Tuple<>(existingClient, existingClientProps)));
        lenient().when(employeeRepository.employeeIdExists("ab123456")).thenReturn(true);
        lenient().when(pendingVerificationUserRepository.findByUsername(UserType.CLIENT, existingClient.getUsername())).thenReturn(Optional.of(new Tuple<>(existingClient, new HashMap<>())));
        lenient().when(pendingVerificationUserRepository.findByUsername(UserType.EMPLOYEE, existingEmployee.getUsername())).thenReturn(Optional.of(new Tuple<>(existingEmployee, new HashMap<>())));
        lenient().when(pendingVerificationUserRepository.findByUsername(UserType.CLIENT, existingEmployee.getUsername())).thenReturn(Optional.of(new Tuple<>(existingEmployee, new HashMap<>())));
        lenient().when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        lenient().when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        //TODO: separate client and employee dummy passwords and salts
        lenient().when(passwordHasher.hash(dummyPasswordLightHash)).thenReturn(dummyPasswordHash);
        lenient().when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        lenient().when(userRepository.getEmployeePasswordHash(existingEmployee.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        lenient().when(passwordHasher.matches(dummyPasswordHash, dummyPasswordHashStored)).thenReturn(true);
        lenient().when(emailEngine.sendTemplatedEmail(
                eq(new EmailMessage(existingClient.getPersonalEmail(), CLIENT_VERIFICATION_EMAIL_SUBJECT, null, null)),
                anyString()
        )).thenReturn(true);
        lenient().when(emailEngine.sendTemplatedEmail(
                eq(new EmailMessage(existingEmployee.getEmployeeEmail(), EMPLOYEE_VERIFICATION_EMAIL_SUBJECT, null, null)),
                anyString()
        )).thenReturn(true);
        lenient().when(pendingVerificationUserRepository.save(existingClient)).thenReturn(true);
        lenient().when(pendingVerificationUserRepository.save(existingEmployee)).thenReturn(true);
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistration("johnmdoe1",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsFailureIfUserNameOrPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(existingClient));
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
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> registrationService.initiateClientRegistration("johnmdoe",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistration_returnsPendingVerificationIfIdentifiersAreValidAndPasswordIsValid() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateClientRegistration("johnmdoe", dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com",
                        "John", "M", "Doe"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsAlreadyRegisteredIfPersonalEmailIsRegistered() {
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(existingClient));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsFailureIfPersonalEmailIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(userRepository.findByUsername(UserType.EMPLOYEE, "johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(pendingVerificationUserRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.empty());
        when(emailEngine.sendTemplatedEmail(
                argThat(msg -> msg.recipient().equals("johnmdoe@outlook.com")),
                anyString()
        )).thenReturn(true);
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        nonRegisteredPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateClientRegistrationForExistingEmployee_returnsPendingVerificationWhenUserIsRegisteredAsClientAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingEmployee));
        when(pendingVerificationUserRepository.save(argThat(pendingUser ->
                pendingUser.getUsername().equals("johnmdoe"))))
                .thenReturn(true);
        when(emailEngine.sendTemplatedEmail(
                argThat(email -> email.recipient().equals("johnmdoe@outlook.com")),
                eq(VERIFICATION_EMAIL_TEMPLATE)))
                .thenReturn(true);
        assertEquals(RegistrationStatus.PENDING_VERIFICATION,
                registrationService.initiateClientRegistrationForExistingEmployee("johnmdoe",
                        dummyPasswordLightHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1", dummyPasswordLightHash,
                        employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfUsernameIsTaken() {
        when(employeeRepository.employeeIdExists("ab987654")).thenReturn(true);
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe",
                        dummyPasswordLightHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsAlreadyRegisteredIfEmployeeEmailIsTaken() {
        when(employeeRepository.employeeIdExists("ab987654")).thenReturn(true);
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistration("ab987654", "johnmdoe1",
                        dummyPasswordLightHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfUserNameIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfEmployeeIdIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(employeeRepository.employeeIdExists("ab123456")).thenReturn(true);
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123456", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));    }

    @Test
    void initiateEmployeeRegistration_returnsFailureIfEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistration("ab123457", "johnmdoe1",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void initiateEmployeeRegistration_throwsIfRoleNotFound() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.empty());
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
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingClient));
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.ALREADY_REGISTERED,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPasswordLightHash, employeeRoleNames, "johnmdoe@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUsernameIsNotFound() {
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        dummyPasswordHash, clientRoleNames, "johnmdoe@outlook.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfUserNameIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingEmployee));
        assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123457", "johnmdoe",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfEmployeeIdIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(existingClient));
        when(employeeRepository.employeeIdExists("ab123456")).thenReturn(true);
        when(pendingVerificationUserRepository.findByUsername(UserType.EMPLOYEE, "johnmdoe")).thenReturn(Optional.empty());
        when(emailEngine.sendTemplatedEmail(
                argThat(msg -> msg.recipient().equals("johnmdoe1@company.com")),
                anyString()
        )).thenReturn(true);
assertEquals(RegistrationStatus.FAILURE,
                registrationService.initiateEmployeeRegistrationForExistingClient("ab123456", "johnmdoe",
                        nonRegisteredPasswordHash, employeeRoleNames, "johnmdoe1@company.com"));
    }

    @Test
    void initiateEmployeeRegistrationForExistingClient_returnsFailureIfEmployeeEmailIsTakenAndPasswordDoesNotMatch() {
        byte[] nonRegisteredPasswordHash = BufferedConverter.toBytes("12345678");
        when(employeeRepository.employeeIdExists("ab123457")).thenReturn(true);
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(existingEmployee));
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
        when(userRepository.findByUsername(UserType.EMPLOYEE,"johnmdoe")).thenReturn(Optional.of(existingClient));
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
        when(pendingVerificationUserRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(new Tuple<>(existingClient, new HashMap<>())));
        when(pendingVerificationUserRepository.getClientRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                UserType.CLIENT, "123457"));
    }

    @Test
    void completeRegistration_returnsFailureIfEmployeeVerificationCodeDoesNotMatch() {
        when(pendingVerificationUserRepository.findByUsername(UserType.EMPLOYEE, "johnmdoe")).thenReturn(Optional.of(new Tuple<>(existingEmployee, new HashMap<>())));
        when(pendingVerificationUserRepository.getEmployeeRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        assertEquals(RegistrationStatus.FAILURE, registrationService.completeRegistration("johnmdoe",
                UserType.EMPLOYEE, "123457"));
    }

    @Test
    void completeRegistration_returnsSuccessIfClientVerificationCodeMatches() {
        when(pendingVerificationUserRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.of(new Tuple<>(existingClient, new HashMap<>())));
        when(pendingVerificationUserRepository.getClientRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        when(pendingVerificationUserRepository.deleteByUuid(any(UUID.class))).thenReturn(true);
        when(userRepository.save(existingClient)).thenReturn(true);
        assertEquals(RegistrationStatus.SUCCESS, registrationService.completeRegistration("johnmdoe",
                UserType.CLIENT, "123456"));
    }

    @Test
    void completeRegistration_returnsSuccessIfEmployeeVerificationCodeMatches() {
        when(pendingVerificationUserRepository.findByUsername(UserType.EMPLOYEE, "johnmdoe")).thenReturn(Optional.of(new Tuple<>(existingClient, new HashMap<>())));
        when(pendingVerificationUserRepository.getEmployeeRegistrationVerificationCode(any(UUID.class)))
                .thenReturn(Optional.of("123456"));
        when(pendingVerificationUserRepository.deleteByUuid(any(UUID.class))).thenReturn(true);
        when(userRepository.save(existingClient)).thenReturn(true);
        assertEquals(RegistrationStatus.SUCCESS, registrationService.completeRegistration("johnmdoe",
                UserType.EMPLOYEE, "123456"));
    }
}
