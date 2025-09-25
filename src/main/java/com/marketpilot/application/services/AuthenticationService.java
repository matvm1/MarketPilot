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
import java.util.UUID;
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

    public AuthenticationStatus initiateClientAuthentication(String usernameOrEmail, char[] passwordLightHash, RoleName roleName) {
        Function<String, Optional<User>> userFinder = identifier -> userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByPersonalEmail(identifier));

        return authenticate(usernameOrEmail, passwordLightHash, roleName,
                userFinder,
                userRepository::getClientPasswordSalt,
                userRepository::getClientPasswordHash);
    }

    public AuthenticationStatus initiateEmployeeAuthentication(String employeeId, char[] lightlyHashedPassword, RoleName roleName) {
        return authenticate(employeeId, lightlyHashedPassword, roleName,
                userRepository::findByEmployeeId,
                userRepository::getEmployeePasswordSalt,
                userRepository::getEmployeePasswordHash);
    }

    private AuthenticationStatus authenticate(String identifier, char[] lightlyHashedPassword, RoleName roleName,
                                              Function<String, Optional<User>> userFinder,
                                              Function<UUID, Optional<char[]>> passwordSaltFinder,
                                              Function<UUID, Optional<char[]>> passwordHashFinder) {
        boolean identifierIsValid = identifier != null && !identifier.isBlank();

        Optional<User> userOptional = userFinder.apply(identifier);

        // check all conditions at once to prevent timing attacks
        boolean userExists = userOptional.isPresent();
        User user = userOptional.orElse(null);

        char[] passwordSalt = userExists ? passwordSaltFinder.apply(user.getUUID()).orElse(null) : null;
        char[] passwordHash;
        try {
            passwordHash = passwordHasher.hash(lightlyHashedPassword, passwordSalt);
        } catch (IllegalArgumentException e) {
            passwordHash = null;
        }
        fillZero(lightlyHashedPassword);
        fillZero(passwordSalt);
        lightlyHashedPassword = null;
        passwordSalt = null;
        char[] dummyPasswordHashStored = "$2a$10$dummyhashtopreventtimingattacksXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".toCharArray();
        char[] passwordHashStored = userExists ? passwordHashFinder.apply(user.getUUID()).orElse(dummyPasswordHashStored) : dummyPasswordHashStored;
        boolean passwordMatches = passwordHasher.matches(passwordHash, passwordHashStored);
        fillZero(passwordHash);
        fillZero(passwordHashStored);
        passwordHash = null;
        passwordHashStored = null;

        boolean hasRole = userExists && userOptional.get().hasRole(roleName);

        if (identifierIsValid && userExists && hasRole && passwordMatches) {
            return twoFactorService.sendChallenge(user.getUsername(), roleName).isPresent()
                    ? AuthenticationStatus.CHALLENGE_SENT
                    : AuthenticationStatus.FAILURE;
        }

        //TODO: A dummy challenge should also be sent
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

    private static void fillZero(char[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = '\0';
    }
}
