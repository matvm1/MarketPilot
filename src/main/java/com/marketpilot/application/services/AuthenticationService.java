package com.marketpilot.application.services;

import com.marketpilot.util.Tuple;
import com.marketpilot.application.dto.auth.AuthenticationResult;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

// TODO: Integration tests
public class AuthenticationService {
    public enum AuthenticationStatus {
        AWAITING_2FA,
        SUCCESS,
        FAILURE
    }

    public enum MfaType {
        TOTP
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TwoFactorService totpService;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, TwoFactorService totpService,
                                 PasswordHasher passwordHasher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.totpService = totpService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    public Tuple<AuthenticationStatus, Optional<UUID>> initiateClientAuthentication(String usernameOrEmail, char[] passwordLightHash, RoleName roleName) {
        Function<String, Optional<User>> userFinder = identifier -> userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByPersonalEmail(identifier));

        return initiateAuthentication(usernameOrEmail, passwordLightHash, roleName,
                userFinder,
                userRepository::getClientPasswordSalt,
                userRepository::getClientPasswordHash,
                UserType.CLIENT);
    }

    public Tuple<AuthenticationStatus, Optional<UUID>> initiateEmployeeAuthentication(String employeeId, char[] passwordLightHash, RoleName roleName) {
        return initiateAuthentication(employeeId, passwordLightHash, roleName,
                userRepository::findByEmployeeId,
                userRepository::getEmployeePasswordSalt,
                userRepository::getEmployeePasswordHash,
                UserType.EMPLOYEE);
    }

    private Tuple<AuthenticationStatus, Optional<UUID>> initiateAuthentication(String identifier, char[] passwordLightHash, RoleName roleName,
                                                              Function<String, Optional<User>> userFinder,
                                                              Function<UUID, Optional<char[]>> passwordSaltFinder,
                                                              Function<UUID, Optional<char[]>> passwordHashFinder,
                                                              UserType userType) {
        boolean identifierIsValid = identifier != null && !identifier.isBlank();

        Optional<User> userOptional = userFinder.apply(identifier);

        // check all conditions at once to prevent timing attacks
        boolean userExists = userOptional.isPresent();
        User user = userOptional.orElse(null);

        char[] passwordSalt = userExists ? passwordSaltFinder.apply(user.getUUID()).orElse(null) : null;
        char[] passwordHash;
        try {
            passwordHash = passwordHasher.hash(passwordLightHash, passwordSalt);
        } catch (IllegalArgumentException e) {
            passwordHash = null;
        }
        fillZero(passwordLightHash);
        fillZero(passwordSalt);
        passwordLightHash = null;
        passwordSalt = null;
        char[] dummyPasswordHashStored = "$2a$10$dummyhashtopreventtimingattacksXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".toCharArray();
        char[] passwordHashStored = userExists ? passwordHashFinder.apply(user.getUUID()).orElse(dummyPasswordHashStored) : dummyPasswordHashStored;
        boolean passwordMatches = passwordHasher.matches(passwordHash, passwordHashStored);
        fillZero(passwordHash);
        fillZero(passwordHashStored);
        passwordHash = null;
        passwordHashStored = null;

        Role authRole = userExists ? user.getRole(roleName) : null;
        boolean hasRole = userExists && authRole != null && authRole.getUserType().equals(userType);

        if (identifierIsValid && userExists && hasRole && passwordMatches) {
            return new Tuple<>(AuthenticationStatus.AWAITING_2FA, Optional.of(user.getUUID()));
        }

        return new Tuple<>(AuthenticationStatus.FAILURE, Optional.empty());
    }

    public AuthenticationStatus completeAuthentication(MfaType mfaType, MfaCredential credentials) {
        if (mfaType == null)
            return AuthenticationStatus.FAILURE;
        if (credentials == null)
            return AuthenticationStatus.FAILURE;

        if (mfaType == MfaType.TOTP) {
            Optional<User> userOptional = userRepository.findByUUID(((TotpCredential)credentials).getUserUuid());
            RoleName roleName = ((TotpCredential)credentials).getRoleName();

            if (userOptional.isPresent() ) {
                User user = userOptional.get();
                Role userRole = user.getRole(roleName);
                UUID userUuid = ((TotpCredential) credentials).getUserUuid();

                if (userRole != null) {
                    Optional<char[]> totpSecretOptional =
                        switch (userRole.getUserType()) {
                            case CLIENT -> userRepository.getClientTotpSecret(userUuid);
                            case EMPLOYEE -> userRepository.getEmployeeTotpSecret(userUuid);
                        };
                    ((TotpCredential) credentials).setSecret(totpSecretOptional.orElse(null));
                    if (totpService.verify(credentials)) {
                        totpSecretOptional.ifPresent(secret -> {
                            fillZero(secret);
                            secret = null;
                        });
                        totpSecretOptional = Optional.empty();
                        credentials = null;
                        if (sessionManager.createSession(new AuthenticationResult(user, userRole)).isPresent())
                            return AuthenticationStatus.SUCCESS;
                    }
                }
            }
        }

        return AuthenticationStatus.FAILURE;
    }

    private static void fillZero(char[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = '\0';
    }
}
