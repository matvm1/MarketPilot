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
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.services.UserFactory;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Optional;

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
    private final UserFactory userFactory;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, TwoFactorService twoFactorService,
                                 PasswordHasher passwordHasher, SessionManager sessionManager, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.twoFactorService = twoFactorService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
        this.userFactory = userFactory;
    }

    public AuthenticationStatus initiateClientAuthentication(String usernameOrClientEmail, String passwordHash, RoleName roleName) {
        if (usernameOrClientEmail == null)
            return AuthenticationStatus.FAILURE;

        Optional<User> userOptional = userRepository.findByUsername(usernameOrClientEmail)
                .or(() -> userRepository.findByPersonalEmail(usernameOrClientEmail));
        if (userOptional.isPresent() && userOptional.get().hasRole(roleName))
            if (passwordHasher.matches(passwordHash, userOptional.get().getClientPasswordHash())) {
                if (twoFactorService.sendChallenge(userOptional.get().getUsername(), roleName).isPresent())
                    return AuthenticationStatus.CHALLENGE_SENT;
        }

        return AuthenticationStatus.FAILURE;
    }

    public AuthenticationStatus initiateEmployeeAuthentication(String employeeId, String passwordHash, RoleName roleName) {
        if (employeeId == null)
            return AuthenticationStatus.FAILURE;
        if (employeeId.isBlank())
            return AuthenticationStatus.FAILURE;

        Optional<User> userOptional = userRepository.findByEmployeeId(employeeId);
        if (userOptional.isPresent() && userOptional.get().hasRole(roleName))
            if (passwordHasher.matches(passwordHash, userOptional.get().getEmployeePasswordHash())) {
                if (twoFactorService.sendChallenge(userOptional.get().getUsername(), roleName).isPresent())
                    return AuthenticationStatus.CHALLENGE_SENT;
        }

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
