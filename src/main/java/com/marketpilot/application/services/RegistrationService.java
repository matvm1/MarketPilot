package com.marketpilot.application.services;

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

    public RegistrationStatus initiateClientRegistration(String username, byte[] passwordLightHash, Set<Role.RoleName> clientRoleNames, String personalEmail,
                                                         String firstName, String middleName, String lastName) {
        UserClientDTO userClientDTO = new UserClientDTO(username, getRolesFromRoleNames(clientRoleNames), personalEmail, firstName, middleName, lastName);
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

    public RegistrationStatus initiateClientRegistrationForExistingEmployee(String username, byte[] passwordLightHash, Set<Role.RoleName> clientRoleNames,
                                                                            String personalEmail) {

        //TODO: Authenticate the employee
        Optional<User> existingEmployeeOptional = userRepository.findByUsername(username);
        if (existingEmployeeOptional.isPresent()) {
            User existingEmployee = existingEmployeeOptional.get();
            UserClientDTO userClientDTO = new UserClientDTO(username, getRolesFromRoleNames(clientRoleNames), personalEmail,
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

    public RegistrationStatus initiateEmployeeRegistration(String employeeId, String username, byte[] passwordLightHash, Set<Role.RoleName> employeeRoleNames,
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

    public RegistrationStatus initiateEmployeeRegistrationForExistingClient(String employeeId, String username, byte[] passwordLightHash,
                                                                            Set<Role.RoleName> employeeRoleNames, String employeeEmail) {
        if (!employeeRepository.employeeIdExists(employeeId))
            return RegistrationStatus.FAILURE;

        //TODO: Authenticate the client
        Optional<User> existingClientOptional = userRepository.findByUsername(username);
        if (existingClientOptional.isPresent()) {
            User existingClient = existingClientOptional.get();
            UserEmployeeDTO userEmployeeDTO = new UserEmployeeDTO(employeeId, username, getRolesFromRoleNames(employeeRoleNames), employeeEmail,
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

    //TODO: Store password hash if registering a new user (instead of matching hash)
    private RegistrationStatus initiateRegistration(UserType registrationUserType,
                                                    User existingUser,
                                                    String identifier1,
                                                    String identifier2,
                                                    UserAbstractDTO newUserAbstractDTO,
                                                    byte[] passwordLightHash,
                                                    Predicate<String> employeeIdFinder,
                                                    Function<UUID, Optional<byte[]>> passwordHashFinder,
                                                    BiFunction<String, String, Optional<User>> userFinder,
                                                    Predicate<User> isRegistrationType,
                                                    BiFunction<User, UserAbstractDTO, User> userFactoryAction,
                                                    String verificationEmail,
                                                    String verificationEmailSubject)
    {
        boolean isEmployeeIdFoundForEmployeeRegistration = registrationUserType == UserType.CLIENT ||
                (registrationUserType == UserType.EMPLOYEE && !employeeIdFinder.test(((UserEmployeeDTO)newUserAbstractDTO).getEmployeeId()));

        boolean identifiersAreValid =
                isEmployeeIdFoundForEmployeeRegistration &&
                        identifier1 != null && !identifier1.isBlank() &&
                        identifier2 != null && !identifier2.isBlank();

        Optional<User> userOptional;

        if (existingUser == null)
            userOptional = userRepository.findByUsername(newUserAbstractDTO.getUsername()).or(() -> userFinder.apply(identifier1, identifier2));
        else
            userOptional = Optional.of(existingUser);

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
        boolean passwordMatches = passwordHasher.matches(passwordLightHash, passwordHashStored);
        fillZero(passwordLightHash);
        fillZero(passwordHash);
        fillZero(passwordHashStored);
        passwordLightHash = null;
        passwordHash = null;
        passwordHashStored = null;

        if (identifiersAreValid && existingUser == null && userExists) {
            if (passwordMatches)
                return RegistrationStatus.ALREADY_REGISTERED;
            else
                return RegistrationStatus.FAILURE;
        }

        if (identifiersAreValid && (existingUser == null || !isRegistrationType.test(user))) {
            user = userFactoryAction.apply(user, newUserAbstractDTO);
            if (emailEngine.sendTemplatedEmail(new EmailMessage(verificationEmail, verificationEmailSubject, null, null),VERIFICATION_EMAIL_TEMPLATE))
                if (pendingVerificationUserRepository.register(registrationUserType, user, passwordHash))
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

    private static void fillZero(byte[] arr) {
        if (arr == null)
            return;
        int len = arr.length;
        for (int i = 0; i < len; ++i)
            arr[i] = (byte) 0;
    }
}
