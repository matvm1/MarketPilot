package com.marketpilot.application.services;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.User;

import java.util.Arrays;
import java.util.Optional;

// TODO: Unit tests
public class AuthenticationService {
    public enum AuthenticationStatus {
        CHALLENGE_SENT,
        SUCCESS,
        FAILURE
    }

    private UserRepository userRepository;
    private TwoFactorService twoFactorService;
    private PasswordHasher passwordHasher;
    private SessionManager sessionManager;

    public AuthenticationService(UserRepository userRepository, TwoFactorService twoFactorService,
                                 PasswordHasher passwordHasher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.twoFactorService = twoFactorService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    //TODO: Hash client-side
    public AuthenticationStatus initiateClientAuthentication(String usernameOrClientEmail, char[] rawPassword) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (usernameOrClientEmail == null)
            return AuthenticationStatus.FAILURE;

        Optional<User> optionalUser = userRepository.findByUsername(usernameOrClientEmail)
                .or(() -> userRepository.findByPersonalEmail(usernameOrClientEmail));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordHasher.matches(passwordHash, user.getClientPasswordHash())) {
                if (twoFactorService.sendChallenge(user).isPresent())
                    return AuthenticationStatus.CHALLENGE_SENT;
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    //TODO: Hash client-side
    public AuthenticationStatus initiateEmployeeAuthentication(String employeeId, char[] rawPassword) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (employeeId == null)
            return AuthenticationStatus.FAILURE;
        if (employeeId.isBlank())
            return AuthenticationStatus.FAILURE;

        Optional<User> optionalUser = userRepository.findByEmployeeId(employeeId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordHasher.matches(passwordHash, user.getEmployeePasswordHash())) {
                if (twoFactorService.sendChallenge(user).isPresent())
                    return AuthenticationStatus.CHALLENGE_SENT;
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    //TODO: code review and unit tests
    public Optional<AuthenticationResult> completeAuthentication(User user, String challenge) {
        Optional<AuthenticationResult> authenticationResult = twoFactorService.verify(user, challenge);
        authenticationResult.ifPresent(result -> sessionManager.createSession(result));
        return authenticationResult;
    }
}
