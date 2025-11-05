package com.marketpilot.application.services;

import com.marketpilot.adapters.persistence.repo.RoleCache;
import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.dto.user.UserAbstractDTO;
import com.marketpilot.application.dto.user.UserClientDTO;
import com.marketpilot.application.dto.user.UserEmployeeDTO;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.domain.repo.EmployeeRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import com.marketpilot.util.VerificationCodeGenerator;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RegistrationService {
    public enum RegistrationStatus {
        SUCCESS,
        FAILURE,
        ALREADY_REGISTERED,
        PENDING_VERIFICATION,
        EXPIRED
    }

    private final UserRepository userRepository;
    private final PendingVerificationUserRepository pendingVerificationUserRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmailEngine emailEngine;
    private final PasswordHasher passwordHasher;
    private final UserFactory userFactory;
    private final RoleCache roleCache;

    private final int VERIFICATION_CODE_LENGTH = 8;
    private final String CLIENT_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Account";
    private final String EMPLOYEE_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Employee Account";
    private final String VERIFICATION_EMAIL_TEMPLATE = "verification_email";
    private final String WELCOME_LETTER_TEMPLATE = "welcome_letter";

    public RegistrationService(UserRepository userRepository,
                               PendingVerificationUserRepository pendingVerificationUserRepository,
                               EmployeeRepository employeeRepository,
                               RoleRepository roleRepository,
                               EmailEngine emailEngine,
                               PasswordHasher passwordHasher,
                               UserFactory userFactory,
                               RoleCache roleCache) {
        this.userRepository = userRepository;
        this.pendingVerificationUserRepository = pendingVerificationUserRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.emailEngine = emailEngine;
        this.passwordHasher = passwordHasher;
        this.userFactory = userFactory;
        this.roleCache = roleCache;
    }

    public RegistrationStatus initiateClientRegistration(String username, byte[] passwordLightHash, Role.RoleName[] clientRoleNames, String personalEmail,
                                                         String firstName, String middleName, String lastName) {
        UserClientDTO userClientDTO = new UserClientDTO(username, roleCache.fetch(clientRoleNames), personalEmail, firstName, middleName, lastName);
        BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByPersonalEmail(identifier2);
        BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) -> userFactory.createClientUser((UserClientDTO) b);

        return initiateRegistration(UserType.CLIENT,
                null,
                username, personalEmail, userClientDTO, passwordLightHash,
                null,
                userRepository::getClientPasswordHash,
                userFinder,
                User::isClient,
                userFactoryAction,
                personalEmail,
                CLIENT_VERIFICATION_EMAIL_SUBJECT);
    }

    // assumes employee has been authenticated
    public RegistrationStatus initiateClientRegistrationForExistingEmployee(String username, byte[] passwordLightHash, Role.RoleName[] clientRoleNames,
                                                                            String personalEmail) {

        Optional<User> existingEmployeeOptional = userRepository.findByUsername(UserType.EMPLOYEE, username);
        if (existingEmployeeOptional.isPresent()) {
            User existingEmployee = existingEmployeeOptional.get();
            UserClientDTO userClientDTO = new UserClientDTO(username, roleCache.fetch(clientRoleNames), personalEmail,
                    existingEmployee.getFirstName(), existingEmployee.getMiddleName(), existingEmployee.getLastName());
            BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByPersonalEmail(identifier2);
            BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) ->
                    userFactory.assignClientAttributes(existingEmployee, (UserClientDTO) b);

            return initiateRegistration(UserType.CLIENT,
                    existingEmployee,
                    username, personalEmail, userClientDTO, passwordLightHash,
                    null,
                    userRepository::getClientPasswordHash,
                    userFinder,
                    User::isClient,
                    userFactoryAction,
                    personalEmail,
                    CLIENT_VERIFICATION_EMAIL_SUBJECT);
        }

        return RegistrationStatus.FAILURE;
    }

    public RegistrationStatus initiateEmployeeRegistration(String employeeId, String username, byte[] passwordLightHash, Role.RoleName[] employeeRoleNames,
                                                           String employeeEmail, String firstName, String middleName, String lastName) {
        UserEmployeeDTO userEmployeeDTO = new UserEmployeeDTO(employeeId, username, roleCache.fetch(employeeRoleNames), employeeEmail,
                firstName, middleName, lastName);
        BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) -> userFactory.createEmployeeUser((UserEmployeeDTO) b);

        return initiateRegistration(UserType.EMPLOYEE,
                null,
                employeeId, employeeEmail, userEmployeeDTO, passwordLightHash,
                employeeRepository::employeeIdExists,
                userRepository::getEmployeePasswordHash,
                userFinder,
                User::isEmployee,
                userFactoryAction,
                employeeEmail,
                EMPLOYEE_VERIFICATION_EMAIL_SUBJECT);
    }

    // assumes client has been authenticated
    public RegistrationStatus initiateEmployeeRegistrationForExistingClient(String employeeId, String username, byte[] passwordLightHash,
                                                                            Role.RoleName[] employeeRoleNames, String employeeEmail) {
        Optional<User> existingClientOptional = userRepository.findByUsername(UserType.CLIENT, username);
        if (existingClientOptional.isPresent()) {
            User existingClient = existingClientOptional.get();
            UserEmployeeDTO userEmployeeDTO = new UserEmployeeDTO(employeeId, username, roleCache.fetch(employeeRoleNames), employeeEmail,
                    existingClient.getFirstName(), existingClient.getMiddleName(), existingClient.getLastName());
            BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByEmployeeId(employeeId)
                    .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
            BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) ->
                    userFactory.assignEmployeeAttributes(existingClient, (UserEmployeeDTO) b);

            return initiateRegistration(UserType.EMPLOYEE,
                    existingClient,
                    employeeId, employeeEmail, userEmployeeDTO, passwordLightHash,
                    employeeRepository::employeeIdExists,
                    userRepository::getEmployeePasswordHash,
                    userFinder,
                    User::isEmployee,
                    userFactoryAction,
                    employeeEmail,
                    EMPLOYEE_VERIFICATION_EMAIL_SUBJECT);
        }

        return RegistrationStatus.FAILURE;
    }

    private RegistrationStatus initiateRegistration(UserType registrationUserType,
                                                    User existingUser,
                                                    String identifier1,
                                                    String identifier2,
                                                    UserAbstractDTO userAbstractDTO,
                                                    byte[] passwordLightHash,
                                                    Predicate<String> employeeIdFinder,
                                                    Function<UUID, Optional<byte[]>> passwordHashFinder,
                                                    BiFunction<String, String, Optional<User>> userFinder,
                                                    Predicate<User> isRegistrationType,
                                                    BiFunction<User, UserAbstractDTO, User> userFactoryAction,
                                                    String verificationEmail,
                                                    String verificationEmailSubject)
    {
        Instant requestTime = Instant.now();
        boolean isEmployeeIdFoundForEmployeeRegistration = registrationUserType == UserType.CLIENT ||
                (registrationUserType == UserType.EMPLOYEE && employeeIdFinder.test(((UserEmployeeDTO)userAbstractDTO).getEmployeeId()));

        boolean identifiersAreValid =
                isEmployeeIdFoundForEmployeeRegistration &&
                        identifier1 != null && !identifier1.isBlank() &&
                        identifier2 != null && !identifier2.isBlank();

        Optional<User> userOptional;

        if (existingUser == null)
            userOptional = userRepository.findByUsername(registrationUserType, userAbstractDTO.getUsername()).or(() -> userFinder.apply(identifier1,
                    identifier2));
        else
            userOptional = Optional.of(existingUser);

        Optional<Tuple<User, Map<String, Object>>> existingPendingVerificationOptional = pendingVerificationUserRepository.findByUsername(registrationUserType,
                userAbstractDTO.getUsername());

        String expirationColumn = registrationUserType == UserType.CLIENT ? "CLIENT_REGISTRATION_EXPIRATION" : "EMPLOYEE_REGISTRATION_EXPIRATION";
        Tuple<User, Map<String, Object>> existingPendingVerification = existingPendingVerificationOptional.orElse(null);

        boolean userExists = userOptional.isPresent();
        User user = userOptional.orElse(null);

        byte[] passwordHash;
        try {
            passwordHash = passwordHasher.hash(passwordLightHash);
        } catch (IllegalArgumentException e) {
            passwordHash = null;
        }
        byte[] dummyPasswordHashStored = BufferedConverter.toBytes("$2a$10$dummyhashtopreventtimingattacksXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        byte[] passwordHashStored = userExists ? passwordHashFinder.apply(user.getUUID()).orElse(dummyPasswordHashStored) : dummyPasswordHashStored;
        boolean passwordMatches;
        try {
            passwordMatches = passwordHasher.matches(passwordLightHash, passwordHashStored);
        }
        catch (Exception e) {
            passwordMatches = false;
        }
        fillZero(passwordLightHash);
        fillZero(passwordHashStored);
        passwordLightHash = null;
        passwordHashStored = null;

        if (identifiersAreValid && ((existingUser == null && userExists) || (existingUser != null && isRegistrationType.test(existingUser)))) {
            if (passwordMatches)
                return RegistrationStatus.ALREADY_REGISTERED;
            else
                return RegistrationStatus.FAILURE;
        }

        if (existingPendingVerification != null) {
            if (((Instant) existingPendingVerification.u().get(expirationColumn)).isBefore(requestTime))
                return RegistrationStatus.EXPIRED;
            else
                return RegistrationStatus.PENDING_VERIFICATION;
        }
        if (userAbstractDTO.isValid() && identifiersAreValid && (existingUser == null || !isRegistrationType.test(user))) {
            try {
                user = userFactoryAction.apply(user, userAbstractDTO);
            }
            catch (IllegalArgumentException e) {
                return RegistrationStatus.FAILURE;
            }
            String verificationCode = VerificationCodeGenerator.generateAlphanumericCode(VERIFICATION_CODE_LENGTH);
            Map<String, Object> emailVars = new HashMap<>();
            emailVars.put("username", user.getUsername());
            emailVars.put("fullName", user.getFullName());
            emailVars.put("userType", registrationUserType);
            emailVars.put("roles", userAbstractDTO.getRoles().stream()
                    .map(role -> role.getRoleName().displayName())
                    .collect(Collectors.joining(", ")));
            emailVars.put("verificationCode", verificationCode);
            if (emailEngine.sendTemplatedEmail(new EmailMessage(verificationEmail, verificationEmailSubject, null, emailVars),VERIFICATION_EMAIL_TEMPLATE)) {
                try {
                    boolean registered = existingUser == null ? pendingVerificationUserRepository.registerNewUser(registrationUserType, user, userAbstractDTO.getRoles(), passwordHash, verificationCode)
                            : pendingVerificationUserRepository.crossRegister(registrationUserType, user, userAbstractDTO.getRoles(), passwordHash, verificationCode);
                    if (registered) {
                        fillZero(passwordHash);
                        passwordHash = null;
                        return RegistrationStatus.PENDING_VERIFICATION;
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    return RegistrationStatus.FAILURE;
                }
            }
        }

        fillZero(passwordHash);
        passwordHash = null;
        return  RegistrationStatus.FAILURE;
    }

    //TODO: Service that runs in the background and removes users from the pending repo if verification period has 
    // expired
    public RegistrationStatus completeRegistration(String username, UserType registrationUserType,
                                                   String verificationCodeAttempt) {
        Instant requestTime = Instant.now();
        if (registrationUserType == null)
            return RegistrationStatus.FAILURE;

        String expirationColumn = registrationUserType == UserType.CLIENT ? "CLIENT_REGISTRATION_EXPIRATION" :
                "EMPLOYEE_REGISTRATION_EXPIRATION";

        Optional<Tuple<User, Map<String, Object>>> userWithPropsOptional = pendingVerificationUserRepository.findByUsername(registrationUserType, username);
        Tuple<User, Map<String, Object>> userWithProps = userWithPropsOptional.orElse(null);
        User user = userWithProps != null ? userWithProps.t() : null;
        Map<String, Object> registrationProperties = userWithProps != null ? userWithProps.u() : null;
        UUID uuid = user != null ? user.getUUID() : null;

        String verificationCode = registrationProperties == null ? "dummyverificationstring34789623123213554129345" :
            switch (registrationUserType) {
                case CLIENT -> (String) registrationProperties.get("CLIENT_REGISTRATION_CODE");
                case EMPLOYEE -> (String) registrationProperties.get("EMPLOYEE_REGISTRATION_CODE");
                default -> null;
        };

        boolean isRegistrationExpired = registrationProperties != null &&
                ((Instant)registrationProperties.get(expirationColumn)).isBefore(requestTime);

        if (!isRegistrationExpired && uuid != null && verificationCode != null && verificationCode.equals(verificationCodeAttempt)) {
            try {
                if (pendingVerificationUserRepository.completeRegistration(registrationUserType, user.getUUID())) {
                        String recipientAddress = switch(registrationUserType) {
                            case CLIENT -> user.getPersonalEmail();
                            case EMPLOYEE -> user.getEmployeeEmail();
                        };
                        Map<String, Object> emailVars = new HashMap<>();
                        emailVars.put("username", user.getUsername());
                        emailVars.put("fullName", user.getFullName());
                        emailVars.put("userType", registrationUserType);
                        // TODO: Config
                        emailVars.put("loginUrl", "https://marketpilot.com/login");
                        emailEngine.sendTemplatedEmail(new EmailMessage(recipientAddress, "Welcome to MarketPilot", null, emailVars),
                                WELCOME_LETTER_TEMPLATE);
                            return RegistrationStatus.SUCCESS;
                    }
            }
            catch (Exception e) {
                return RegistrationStatus.FAILURE;
            }
        }

        if (isRegistrationExpired)
            return RegistrationStatus.EXPIRED;
        return RegistrationStatus.FAILURE;
    }

    private static void fillZero(byte[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = (byte) 0;
    }
}
