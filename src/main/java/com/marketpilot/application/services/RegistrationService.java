package com.marketpilot.application.services;

import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.dto.user.UserAbstractDTO;
import com.marketpilot.application.dto.user.UserClientDTO;
import com.marketpilot.application.dto.user.UserEmployeeDTO;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.persistence.EmployeeRepository;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class RegistrationService {
    public enum RegistrationStatus {
        SUCCESS,
        FAILURE,
        ALREADY_REGISTERED,
        PENDING_VERIFICATION
    }

    private final UserRepository userRepository;
    private final PendingVerificationUserRepository pendingVerificationUserRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmailEngine emailEngine;
    private final PasswordHasher passwordHasher;
    private final UserFactory userFactory;
    private final String CLIENT_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Account";
    private final String EMPLOYEE_VERIFICATION_EMAIL_SUBJECT = "Welcome to MarketPilot! Verify Your Email to Activate Your Employee Account";
    private final String VERIFICATION_EMAIL_TEMPLATE = "verification_email.html";

    public RegistrationService(UserRepository userRepository,
                               PendingVerificationUserRepository pendingVerificationUserRepository,
                               EmployeeRepository employeeRepository,
                               RoleRepository roleRepository,
                               EmailEngine emailEngine, PasswordHasher passwordHasher, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.pendingVerificationUserRepository = pendingVerificationUserRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.emailEngine = emailEngine;
        this.passwordHasher = passwordHasher;
        this.userFactory = userFactory;
    }

    public RegistrationStatus initiateClientRegistration(String username, char[] passwordLightHash, Set<Role.RoleName> clientRoleNames, String personalEmail,
                                                         String firstName, String middleName, String lastName) {
        UserClientDTO userClientDTO = new UserClientDTO(username, getRolesFromRoleNames(clientRoleNames), personalEmail, firstName, middleName, lastName);
        BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByPersonalEmail(identifier2);
        BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) -> userFactory.createClientUser((UserClientDTO) b);

        //TODO:supply password
        return initiateRegistration(false, username, personalEmail, userClientDTO, passwordLightHash,
                null,
                userRepository::getClientPasswordSalt,
                userRepository::getClientPasswordHash,
                userFinder,
                User::isClient,
                userFactoryAction,
                personalEmail,
                CLIENT_VERIFICATION_EMAIL_SUBJECT);
    }

    public RegistrationStatus initiateClientRegistrationForExistingEmployee(String username, char[] passwordLightHash, Set<Role.RoleName> clientRoleNames,
                                                                            String personalEmail) {
        Optional<User> existingEmployeeOptional = userRepository.findByUsername(username)
                .or(() -> userRepository.findByPersonalEmail(personalEmail));
        if (existingEmployeeOptional.isPresent()) {
            User existingEmployee = existingEmployeeOptional.get();
            UserClientDTO userClientDTO = new UserClientDTO(username, getRolesFromRoleNames(clientRoleNames), personalEmail,
                    existingEmployee.getFirstName(), existingEmployee.getMiddleName(), existingEmployee.getLastName());
            //TODO: user already found, pass user object
            BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByPersonalEmail(identifier2);
            BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) ->
                    userFactory.assignClientAttributes(existingEmployee, (UserClientDTO) b);

            return initiateRegistration(true, username, personalEmail, userClientDTO, passwordLightHash,
                    null,
                    userRepository::getClientPasswordSalt,
                    userRepository::getClientPasswordHash,
                    userFinder,
                    User::isClient,
                    userFactoryAction,
                    personalEmail,
                    CLIENT_VERIFICATION_EMAIL_SUBJECT);
        }

        return RegistrationStatus.FAILURE;
    }

    public RegistrationStatus initiateEmployeeRegistration(String employeeId, String username, char[] passwordLightHash, Set<Role.RoleName> employeeRoleNames,
                                                           String employeeEmail, String firstName, String middleName, String lastName) {
        if (!employeeRepository.employeeIdExists(employeeId))
            return RegistrationStatus.FAILURE;

        Optional<User> userOptional = userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        UserEmployeeDTO userEmployeeDTO = new UserEmployeeDTO(employeeId, username, getRolesFromRoleNames(employeeRoleNames), employeeEmail,
                firstName, middleName, lastName);
        BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) -> userFactory.createEmployeeUser((UserEmployeeDTO) b);

        //TODO:supply password
        return initiateRegistration(false, employeeId, employeeEmail, userEmployeeDTO, passwordLightHash,
                employeeRepository::employeeIdExists,
                userRepository::getEmployeePasswordSalt,
                userRepository::getEmployeePasswordHash,
                userFinder,
                User::isEmployee,
                userFactoryAction,
                employeeEmail,
                EMPLOYEE_VERIFICATION_EMAIL_SUBJECT);
    }

    public RegistrationStatus initiateEmployeeRegistrationForExistingClient(String employeeId, String username, char[] passwordLightHash,
                                                                            Set<Role.RoleName> employeeRoleNames, String employeeEmail) {
        if (!employeeRepository.employeeIdExists(employeeId))
            return RegistrationStatus.FAILURE;

        Optional<User> existingClientOptional = userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        if (existingClientOptional.isPresent()) {
            User existingClient = existingClientOptional.get();
            UserEmployeeDTO userEmployeeDTO = new UserEmployeeDTO(employeeId, username, getRolesFromRoleNames(employeeRoleNames), employeeEmail,
                    existingClient.getFirstName(), existingClient.getMiddleName(), existingClient.getLastName());
            //TODO: user already found, pass user object
            BiFunction<String, String, Optional<User>> userFinder = (identifier1, identifier2) -> userRepository.findByEmployeeId(employeeId)
                    .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
            BiFunction<User, UserAbstractDTO, User> userFactoryAction = (a, b) ->
                    userFactory.assignEmployeeAttributes(existingClient, (UserEmployeeDTO) b);

            return initiateRegistration(true, employeeId, employeeEmail, userEmployeeDTO, passwordLightHash,
                    employeeRepository::employeeIdExists,
                    userRepository::getEmployeePasswordSalt,
                    userRepository::getEmployeePasswordHash,
                    userFinder,
                    User::isEmployee,
                    userFactoryAction,
                    employeeEmail,
                    EMPLOYEE_VERIFICATION_EMAIL_SUBJECT);
        }

        return RegistrationStatus.FAILURE;
    }

    //TODO: Store password hash if registering a new user (instead of matching hash)
    private RegistrationStatus initiateRegistration(boolean isForExistingUser,
                                                    String identifier1,
                                                    String identifier2,
                                                    UserAbstractDTO userAbstractDTO,
                                                    char[] passwordLightHash,
                                                    Predicate<String> employeeIdFinder,
                                                    Function<UUID, Optional<char[]>> passwordSaltFinder,
                                                    Function<UUID, Optional<char[]>> passwordHashFinder,
                                                    BiFunction<String, String, Optional<User>> userFinder,
                                                    Predicate<User> isRegistrationType,
                                                    BiFunction<User, UserAbstractDTO, User> userFactoryAction,
                                                    String verificationEmail,
                                                    String verificationEmailSubject)
    {
        if (employeeIdFinder != null)
            if (!employeeIdFinder.test(((UserEmployeeDTO)userAbstractDTO).getEmployeeId()))
                return RegistrationStatus.FAILURE;

        //TODO: validate identifier2
        boolean identifierIsValid = identifier1 != null && !identifier1.isBlank();

        Optional<User> userOptional = userRepository.findByUsername(userAbstractDTO.getUsername())
                .or(() -> userFinder.apply(identifier1, identifier2));

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

        if (identifierIsValid && !isForExistingUser && userExists) {
            if (passwordMatches)
                return RegistrationStatus.ALREADY_REGISTERED;
            else
                return RegistrationStatus.FAILURE;
        }

        //TODO: add && passwordMatches -> user must authenticate
        if (isForExistingUser && identifierIsValid && userExists && isRegistrationType.test(user)) {
            user = userFactoryAction.apply(user, userAbstractDTO);
            if (emailEngine.sendTemplatedEmail(new EmailMessage(verificationEmail, verificationEmailSubject, null, null),VERIFICATION_EMAIL_TEMPLATE))
                if (pendingVerificationUserRepository.save(user))
                    return RegistrationStatus.PENDING_VERIFICATION;
        }

        if (!isForExistingUser && identifierIsValid) {
            user = userFactoryAction.apply(user, userAbstractDTO);
            if (emailEngine.sendTemplatedEmail(new EmailMessage(verificationEmail, verificationEmailSubject, null, null),VERIFICATION_EMAIL_TEMPLATE))
                if (pendingVerificationUserRepository.save(user))
                    return RegistrationStatus.PENDING_VERIFICATION;
        }

        return  RegistrationStatus.FAILURE;
    }

    //TODO: Service that runs in the background and removes users from the pending repo if verification period has 
    // expired
    public RegistrationStatus completeRegistration(String username, UserType registrationUserType,
                                                   String verificationCodeAttempt) {
        if (registrationUserType == null)
            return RegistrationStatus.FAILURE;

        Optional<User> userOptional = pendingVerificationUserRepository.findByUsername(username);
        if (userOptional.isEmpty())
            return RegistrationStatus.FAILURE;

        User user = userOptional.get();
        UUID uuid;
        try {
            uuid = user.getUUID();
        } catch (IllegalStateException e) {
            return RegistrationStatus.FAILURE;
        }

        Optional<String> verificationCode = switch (registrationUserType) {
            case CLIENT -> pendingVerificationUserRepository.getClientRegistrationVerificationCode(uuid);
            case EMPLOYEE -> pendingVerificationUserRepository.getEmployeeRegistrationVerificationCode(uuid);
            default -> Optional.empty();
        };
        if (verificationCode.isPresent() && verificationCode.get().equals(verificationCodeAttempt)) {
            try {
                if (pendingVerificationUserRepository.deleteByUUID(uuid))
                    if (userRepository.save(user)) {
                        String recipientAddress = switch(registrationUserType) {
                            case CLIENT -> user.getPersonalEmail();
                            case EMPLOYEE -> user.getEmployeeEmail();
                        };
                        emailEngine.sendTemplatedEmail(new EmailMessage(recipientAddress, "Welcome to MarketPilot",
                                null, null), "welcome_letter.html");
                        return RegistrationStatus.SUCCESS;
                    }
            }
            catch (Exception e) {
                return RegistrationStatus.FAILURE;
            }
        }

        return RegistrationStatus.FAILURE;
    }

    //TODO: safely handle role not found
    private Set<Role> getRolesFromRoleNames(Set<Role.RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : roleNames) {
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
            Role role = optionalRole.orElseThrow(() -> new NoSuchElementException(roleName + " role not found."));
            roles.add(role);
        }

        return roles;
    }

    private static void fillZero(char[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = '\0';
    }
}
