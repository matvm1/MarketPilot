package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.application.services.AuthenticationService.AuthenticationStatus;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private TwoFactorService twoFactorService;
    @Mock private PasswordHasher passwordHasher;
    @Mock private SessionManager sessionManager;

    private AuthenticationService authenticationService;

    private User existingClient;
    private User existingEmployee;

    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;

    private UserFactory userFactory;

    // use dummyPasswordLightHash as argument to all authentication calls
    private byte[] dummyPasswordLightHash;
    private final byte[] dummyPasswordHashStored = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        authenticationService = new AuthenticationService(userRepository, twoFactorService, passwordHasher, sessionManager);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);

        existingClient = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        existingEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");

        dummyPasswordLightHash = BufferedConverter.toBytes("nc36784gfyu43vbf7623frtycwdvtyuawjcevdfyu12b367821f");
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameoremailIsNull() {
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication(null, dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameoremailNotFound(){
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.empty());
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.empty());
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.of(existingClient));
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.of(existingClient));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.Admin));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.Admin));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfPasswordDoesNotMatch(){
        byte[] dummyPasswordHashStored = BufferedConverter.toBytes("sdhjfrgbjh24bqw2bsdf099$n@12!hg1");
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe")).thenReturn(Optional.ofNullable(existingClient));
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfUsernameAndPasswordExist() {
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe").or(() -> userRepository.findByPersonalEmail("johnmdoe")))
                .thenReturn(Optional.of(existingClient));
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordLightHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(Tuple.of(AuthenticationStatus.AWAITING_2FA, Optional.of(existingClient.getUUID())),
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfEmailAndPasswordExist() {
        when(userRepository.findByUsername(UserType.CLIENT, "johnmdoe@outlook.com").or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com")))
                .thenReturn(Optional.of(existingClient));
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordLightHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(Tuple.of(AuthenticationStatus.AWAITING_2FA, Optional.of(existingClient.getUUID())),
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdIsNull() {
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateEmployeeAuthentication(null, dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdNotFound(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.empty());
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Admin));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfPasswordDoesNotMatch() {
        byte[] dummyPasswordHashStored = BufferedConverter.toBytes("sdhjfrgbjh24bqw2bsdf099$n@12!hg1");
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.getEmployeePasswordHash(existingEmployee.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        assertEquals(Tuple.of(AuthenticationStatus.FAILURE, Optional.empty()),
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsChallengeSentIfEmployeeIdAndPasswordExist() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.getEmployeePasswordHash(existingEmployee.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordLightHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(Tuple.of(AuthenticationStatus.AWAITING_2FA, Optional.of(existingEmployee.getUUID())),
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    @Tag("noPasswordByteErasure")
    void completeAuthentication_returnsFailure_ifCredentialsIsNull() {
        assertEquals(AuthenticationStatus.FAILURE, authenticationService.completeAuthentication(
                UUID.randomUUID(),
                TestRoles.PERSONAL_INVESTOR_ROLE,
                null));
    }

    @AfterEach
    void invariants(TestInfo testInfo) {
        System.out.println(testInfo.getTags());
        if(!testInfo.getTags().contains("noPasswordByteErasure"))
            for (byte b: dummyPasswordLightHash)
                assertEquals((byte) 0, b);
    }
}
