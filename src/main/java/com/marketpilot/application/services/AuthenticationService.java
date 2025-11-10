package com.marketpilot.application.services;

import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.domain.entities.auth.UserType;
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

    private final UserRepository userRepository;
    private final TwoFactorService totpService;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    public AuthenticationService(UserRepository userRepository, TwoFactorService totpService,
                                 PasswordHasher passwordHasher, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    public AuthenticationService(UserRepository userRepository, TwoFactorService totpService,
                                 PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.passwordHasher = passwordHasher;
        this.sessionManager = null;
    }

    public Tuple<AuthenticationStatus, Optional<AuthenticationContext>> initiateClientAuthentication(String usernameOrEmail, byte[] passwordLightHash, RoleName roleName) {
        Function<String, Optional<User>> userFinder = identifier -> userRepository.findByUsername(UserType.CLIENT, identifier)
                .or(() -> userRepository.findByPersonalEmail(identifier));

        return initiateAuthentication(usernameOrEmail, passwordLightHash, roleName,
                userFinder,
                userRepository::getClientPasswordHash,
                UserType.CLIENT);
    }

    public Tuple<AuthenticationStatus, Optional<AuthenticationContext>> initiateEmployeeAuthentication(String employeeId, byte[] passwordLightHash, RoleName roleName) {
        return initiateAuthentication(employeeId, passwordLightHash, roleName,
                userRepository::findByEmployeeId,
                userRepository::getEmployeePasswordHash,
                UserType.EMPLOYEE);
    }

    private Tuple<AuthenticationStatus, Optional<AuthenticationContext>> initiateAuthentication(
            String identifier, byte[] passwordLightHash,
            RoleName roleName,
            Function<String, Optional<User>> userFinder,
            Function<UUID, Optional<byte[]>> passwordHashFinder,
            UserType userType) {
        boolean identifierIsValid = identifier != null && !identifier.isBlank();

        Optional<User> userOptional = userFinder.apply(identifier);

        // check all conditions at once to prevent timing attacks
        boolean userExists = userOptional.isPresent();
        User user = userOptional.orElse(null);

        byte[] dummyPasswordHashStored = BufferedConverter.toBytes("$2a$10$dummyhashtopreventtimingattacksXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        byte[] passwordHashStored = userExists ? passwordHashFinder.apply(user.getUUID()).orElse(dummyPasswordHashStored) : dummyPasswordHashStored;
        boolean passwordMatches = passwordHasher.matches(passwordLightHash, passwordHashStored);
        fillZero(passwordLightHash);
        fillZero(passwordHashStored);
        passwordLightHash = null;
        passwordHashStored = null;

        Role authRole = userExists ? user.getRole(roleName) : null;
        boolean hasRole = userExists && authRole != null && authRole.getUserType().equals(userType);

        if (identifierIsValid && userExists && hasRole && passwordMatches) {
            return new Tuple<>(AuthenticationStatus.AWAITING_2FA, Optional.of(new AuthenticationContext(user, authRole)));
        }

        return new Tuple<>(AuthenticationStatus.FAILURE, Optional.empty());
    }

    public AuthenticationStatus completeAuthentication(User user, Role role, MfaCredential credentials) {
        boolean identifierAreValid = user != null && role != null;

        MfaType mfaType = userRepository.getMfaType(user != null ? user.getUUID() : null)
                .orElse(null);

        boolean isAuthenticated = false;

        if (identifierAreValid) {
            if (mfaType == MfaType.NONE) {
                credentials = null;
                isAuthenticated = true;
            } else {
                if (credentials != null) {
                    if (mfaType == MfaType.TOTP && credentials instanceof TotpCredential) {
                        Optional<char[]> totpSecretOptional =
                                switch (role.getUserType()) {
                                    case CLIENT -> userRepository.getClientTotpSecret(user.getUUID());
                                    case EMPLOYEE -> userRepository.getEmployeeTotpSecret(user.getUUID());
                                };
                        ((TotpCredential) credentials).setSecret(totpSecretOptional.orElse(null));
                        if (totpService.verify(credentials)) {
                            totpSecretOptional.ifPresent(secret -> {
                                fillZero(secret);
                                secret = null;
                            });
                            totpSecretOptional = Optional.empty();
                            credentials = null;
                            isAuthenticated = true;
                        }
                    }
                }
            }
        }

        if (isAuthenticated) {
            if (sessionManager == null || sessionManager.createSession(new AuthenticationContext(user, role)).isPresent())
                return AuthenticationStatus.SUCCESS;
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

    private static void fillZero(byte[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = (byte) 0;
    }
}
