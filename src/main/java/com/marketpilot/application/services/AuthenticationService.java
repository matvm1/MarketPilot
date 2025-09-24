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

    //TODO: Hash client-side
    public AuthenticationStatus initiateClientAuthentication(String usernameOrClientEmail, char[] rawPassword, RoleName roleName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (usernameOrClientEmail == null)
            return AuthenticationStatus.FAILURE;

        Optional<User> optionalUser = userRepository.findByUsername(usernameOrClientEmail)
                .or(() -> userRepository.findByPersonalEmail(usernameOrClientEmail));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.hasRole(roleName))
                if (passwordHasher.matches(passwordHash, user.getClientPasswordHash())) {
                    if (twoFactorService.sendChallenge(user.getUsername(), roleName).isPresent())
                        return AuthenticationStatus.CHALLENGE_SENT;
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    //TODO: Hash client-side
    public AuthenticationStatus initiateEmployeeAuthentication(String employeeId, char[] rawPassword, RoleName roleName) {
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
            if (user.hasRole(roleName))
                if (passwordHasher.matches(passwordHash, user.getEmployeePasswordHash())) {
                    if (twoFactorService.sendChallenge(user.getUsername(), roleName).isPresent())
                        return AuthenticationStatus.CHALLENGE_SENT;
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    public AuthenticationStatus completeAuthentication(String username, RoleName roleName, String challenge) {
        AuthenticationStatus authenticationStatus = twoFactorService.verify(username, roleName, challenge);
        if (authenticationStatus == AuthenticationStatus.SUCCESS) {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.hasRole(roleName))
                    if (sessionManager.createSession(new AuthenticationResult(user, getRole(roleName))).isPresent())
                        return AuthenticationStatus.SUCCESS;
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    private Role getRole(RoleName roleName) {
        Optional<Role> roleOptional = roleRepository.findByRoleName(roleName);
        return roleOptional.orElse(null);
    }
}
