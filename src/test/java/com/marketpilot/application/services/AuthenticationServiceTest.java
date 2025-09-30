package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.application.services.AuthenticationService.AuthenticationStatus;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
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
    private char[] dummyPasswordLightHash;
    private final char[] dummySalt = "b234vhnosd9021bhj23vsdb#nkjb$bnjk32!mjkhn*msdhjb3493bn".toCharArray();
    private final char[] dummyPasswordHash = "xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3".toCharArray();
    private final char[] dummyPasswordHashStored = dummyPasswordHash.clone();

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        authenticationService = new AuthenticationService(userRepository, roleRepository, twoFactorService, passwordHasher, sessionManager);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);

        existingClient = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
        existingEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", "johnmdoe@company.com",
                "John", "M", "Doe");

        dummyPasswordLightHash = "nc36784gfyu43vbf7623frtycwdvtyuawjcevdfyu12b367821f".toCharArray();
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameoremailIsNull() {
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication(null, dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameoremailNotFound(){
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.empty());
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.empty());
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.of(existingClient));
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.of(existingClient));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.Admin));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.Admin));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfPasswordDoesNotMatch(){
        char[] dummyPasswordHashStored = "sdhjfrgbjh24bqw2bsdf099$n@12!hg1".toCharArray();
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.ofNullable(existingClient));
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfUsernameAndPasswordExist() {
        when(userRepository.findByUsername("johnmdoe").or(() -> userRepository.findByPersonalEmail("johnmdoe")))
                .thenReturn(Optional.of(existingClient));
        when(userRepository.getClientPasswordSalt(existingClient.getUUID())).thenReturn(Optional.of(dummySalt));
        when(passwordHasher.hash(dummyPasswordLightHash, dummySalt)).thenReturn(dummyPasswordHash);
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(AuthenticationStatus.AWAITING_2FA,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfEmailAndPasswordExist() {
        when(userRepository.findByUsername("johnmdoe@outlook.com").or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com")))
                .thenReturn(Optional.of(existingClient));
        when(userRepository.getClientPasswordSalt(existingClient.getUUID())).thenReturn(Optional.of(dummySalt));
        when(passwordHasher.hash(dummyPasswordLightHash, dummySalt)).thenReturn(dummyPasswordHash);
        when(userRepository.getClientPasswordHash(existingClient.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(AuthenticationStatus.AWAITING_2FA,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordLightHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdIsNull() {
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication(null, dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdNotFound(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.empty());
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Admin));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfPasswordDoesNotMatch() {
        char[] dummyPasswordHashStored = "sdhjfrgbjh24bqw2bsdf099$n@12!hg1".toCharArray();
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.getEmployeePasswordHash(existingEmployee.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsChallengeSentIfEmployeeIdAndPasswordExist() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(userRepository.getEmployeePasswordSalt(existingEmployee.getUUID())).thenReturn(Optional.of(dummySalt));
        when(passwordHasher.hash(dummyPasswordLightHash, dummySalt)).thenReturn(dummyPasswordHash);
        when(userRepository.getEmployeePasswordHash(existingEmployee.getUUID())).thenReturn(Optional.of(dummyPasswordHashStored));
        when(passwordHasher.matches(dummyPasswordHash, dummyPasswordHashStored)).thenReturn(true);
        assertEquals(AuthenticationStatus.AWAITING_2FA,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordLightHash, RoleName.Analyst));
    }

    @AfterEach
    void invariants() {
        for (char c: dummyPasswordLightHash)
            assertEquals('\0', c);
    }
}
