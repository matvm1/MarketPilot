package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.application.services.AuthenticationService.AuthenticationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private TwoFactorService twoFactorService;
    @Mock private PasswordHasher passwordHasher;
    @Mock private SessionManager sessionManager;

    private AuthenticationService authenticationService;

    private char[] dummyPassword = new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
    private static final String dummyPasswordHash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, twoFactorService, passwordHasher,
                sessionManager);
        lenient().when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameClientEmailIsNull() {
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication(null, dummyPassword));
    }

    @Test
    void initiateClientAuthentication_returnsFailureIfUsernameOrClientEmailNotFound(){
        when(userRepository.findByUsername("johnmdoe")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe"))).thenReturn(Optional.empty());
        when(userRepository.findByUsername("johnmdoe@outlook.com")
                .or(() -> userRepository.findByPersonalEmail("johnmdoe@outlook.com"))).thenReturn(Optional.empty());
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe", dummyPassword));
        assertEquals(AuthenticationStatus.FAILURE,
                authenticationService.initiateClientAuthentication("johnmdoe@outlook.com", dummyPassword));
    }

    @Test
    void initiateClientAuthentication_returnsChallengeSentIfUsernameOrClientEmailAndPasswordExist() {

    }
}
