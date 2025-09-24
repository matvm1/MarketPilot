package com.marketpilot.application.services;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;
import java.util.function.Function;

// TODO: Integration tests
public class AuthenticationService {
    public enum AuthenticationStatus {
        CHALLENGE_SENT,
        SUCCESS,
        FAILURE
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TwoFactorService twoFactorService;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, TwoFactorService twoFactorService,
                                 PasswordHasher passwordHasher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.twoFactorService = twoFactorService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    public AuthenticationStatus initiateClientAuthentication(String usernameOrEmail, String passwordHash, RoleName roleName) {
        if (usernameOrEmail == null)
            return AuthenticationStatus.FAILURE;

        Function<String, Optional<User>> userFinder = identifier -> userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByPersonalEmail(identifier));

        return authenticate(usernameOrEmail, passwordHash, roleName, userFinder, User::getClientPasswordHash);
    }

    public AuthenticationStatus initiateEmployeeAuthentication(String employeeId, String passwordHash, RoleName roleName) {
        if (employeeId == null)
            return AuthenticationStatus.FAILURE;
        if (employeeId.isBlank())
            return AuthenticationStatus.FAILURE;

        return authenticate(employeeId, passwordHash, roleName, userRepository::findByEmployeeId, User::getEmployeePasswordHash);
    }

    private AuthenticationStatus authenticate(String identifier, String passwordHash, RoleName roleName,
                                              Function<String, Optional<User>> userFinder, Function<User, String> passwordHashGetter) {
        Optional<User> userOptional = userFinder.apply(identifier);
        if (userOptional.isPresent() && userOptional.get().hasRole(roleName))
            if (passwordHasher.matches(passwordHash, passwordHashGetter.apply(userOptional.get())))
                if (twoFactorService.sendChallenge(userOptional.get().getUsername(), roleName).isPresent())
                    return AuthenticationStatus.CHALLENGE_SENT;

        return AuthenticationStatus.FAILURE;
    }

    public AuthenticationStatus completeAuthentication(String username, RoleName roleName, String challenge) {
        AuthenticationStatus authenticationStatus = twoFactorService.verify(username, roleName, challenge);
        if (authenticationStatus == AuthenticationStatus.SUCCESS) {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent() && userOptional.get().hasRole(roleName))
                    if (sessionManager.createSession(new AuthenticationResult(userOptional.get(), getRole(roleName))).isPresent())
                        return AuthenticationStatus.SUCCESS;
        }

        return AuthenticationStatus.FAILURE;
    }

    private Role getRole(RoleName roleName) {
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        return roleOptional.orElse(null);
    }
}
