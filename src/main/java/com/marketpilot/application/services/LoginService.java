package com.marketpilot.application.services;

import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;
import com.marketpilot.application.ports.auth.AuthenticationService;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.User;

import java.security.SecureRandom;
import java.util.Optional;

public class LoginService {
    public enum LoginStatus {
        CHALLENGE_SENT,
        SUCCESS,
        FAILURE
    }

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    //TODO: completeLoginService() and employee login flow
    //TODO: Unit tests
    public LoginService(UserRepository userRepository, AuthenticationService authenticationService,
                        PasswordHasher passwordHasher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    public LoginStatus initiateClientLogin(String usernameOrClientEmail, char[] rawPassword) {
        if (usernameOrClientEmail == null)
            throw new IllegalArgumentException("usernameOrClientEmail cannot be null");
        if (usernameOrClientEmail.isBlank())
            throw new IllegalArgumentException("usernameOrClientEmail cannot be blank");

        Optional<User> optionalUser = userRepository.findByUsername(usernameOrClientEmail)
                        .or(() -> userRepository.findByPersonalEmail(usernameOrClientEmail));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordHasher.matches(rawPassword, user.getPasswordHash())) {
                authenticationService.sendChallenge(user);
                return LoginStatus.CHALLENGE_SENT;
            }
        }

        return LoginStatus.FAILURE;
    }
}
