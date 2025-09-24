package com.marketpilot.application.services;

import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;
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
import com.marketpilot.domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
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
    private final String dummyPasswordHash = "password";
    private Set<RoleName> clientRoleNames;
    private Set<RoleName> employeeRoleNames;
    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;

    private UserFactory userFactory;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        authenticationService = new AuthenticationService(userRepository, roleRepository, twoFactorService, passwordHasher,
                sessionManager, userFactory);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);

        existingClient = userFactory.createClientUser(clientRoles, "johnmdoe", dummyPasswordHash,
                "johnmdoe@outlook.com", "John", "M", "Doe");
        existingEmployee = userFactory.createEmployeeUser("ab123456", employeeRoles, "johnmdoe", dummyPasswordHash,
                "johnmdoe@company.com", "John", "M", "Doe");
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameClientEmailIsNull() {
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication(null, dummyPasswordHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameOrClientEmailNotFound(){
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.empty());
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.empty());
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordHash, RoleName.PersonalInvestor));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.of(existingClient));
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.of(existingClient));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordHash, RoleName.Admin));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordHash, RoleName.Admin));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfPasswordDoesNotMatch(){
        String wrongPassword = "12345678";
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.of(existingClient));
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.of(existingClient));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", wrongPassword, RoleName.PersonalInvestor));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", wrongPassword, RoleName.PersonalInvestor));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfUsernameOrClientEmailAndPasswordExist() {
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.of(existingClient));
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.of(existingClient));
        when(passwordHasher.matches(dummyPasswordHash, existingClient.getClientPasswordHash()))
                .thenReturn(true);
        when(twoFactorService.sendChallenge(existingClient.getUsername(), RoleName.PersonalInvestor))
                .thenReturn(Optional.of(new TwoFactorAuthenticationChallenge(existingClient.getUsername(), "123456")));
        assertEquals(AuthenticationStatus.CHALLENGE_SENT,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPasswordHash, RoleName.PersonalInvestor));
        assertEquals(AuthenticationStatus.CHALLENGE_SENT,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPasswordHash, RoleName.PersonalInvestor));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdIsNull() {
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication(null, dummyPasswordHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfEmployeeIdNotFound(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.empty());
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordHash, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfUserDoesNotHaveRole(){
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordHash, RoleName.Admin));
    }

    @Test
    void initiateEmployeeAuthentication_returnsFailureIfPasswordDoesNotMatch() {
        String wrongPassword = "12345678";
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateEmployeeAuthentication("ab123456", wrongPassword, RoleName.Analyst));
    }

    @Test
    void initiateEmployeeAuthentication_returnsChallengeSentIfEmployeeIdAndPasswordExist() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(existingEmployee));
        when(passwordHasher.matches(dummyPasswordHash, existingEmployee.getEmployeePasswordHash()))
                .thenReturn(true);
        when(twoFactorService.sendChallenge(existingEmployee.getUsername(), RoleName.Analyst))
                .thenReturn(Optional.of(new TwoFactorAuthenticationChallenge(existingEmployee.getUsername(), "123456")));
        assertEquals(AuthenticationStatus.CHALLENGE_SENT,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordHash, RoleName.Analyst));
        assertEquals(AuthenticationStatus.CHALLENGE_SENT,
                authenticationService.initiateEmployeeAuthentication("ab123456", dummyPasswordHash, RoleName.Analyst));
    }
}
