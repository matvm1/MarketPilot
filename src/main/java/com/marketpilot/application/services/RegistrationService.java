package com.marketpilot.application.services;

import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.persistence.EmployeeRepository;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.services.UserFactory;

import java.util.*;

public class RegistrationService {
    public enum RegistrationResult {
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

    public RegistrationResult initiateClientRegistration(String username, char[] rawPassword,
                               Set<Role.RoleName> clientRoleNames, String personalEmail,
                               String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        Optional<User> userOptional = userRepository.findByUsername(username)
                .or(() -> userRepository.findByPersonalEmail(personalEmail));
        if (userOptional.isPresent()) {
            if (passwordHash.equals(userOptional.get().getClientPasswordHash()))
                return RegistrationResult.ALREADY_REGISTERED;
            else
                return RegistrationResult.FAILURE;
        }

        User newUser = userFactory.createClientUser(getRolesFromRoleNames(clientRoleNames), username,
                passwordHash, personalEmail, firstName, middleName, lastName);
        try {
            if (emailEngine.sendTemplatedEmail(new EmailMessage(personalEmail,
                    "Verify your MarketPilot account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                if (pendingVerificationUserRepository.save(newUser))
                    return RegistrationResult.PENDING_VERIFICATION;
        }
        catch (Exception e) {
            return RegistrationResult.FAILURE;
        }

        return RegistrationResult.FAILURE;
    }

    public RegistrationResult initiateClientRegistrationForExistingEmployee(String username, char[] rawPassword,
                                                                            Set<Role.RoleName> clientRoleNames,
                                                                            String personalEmail) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        Optional<User> existingEmployeeOptional = userRepository.findByUsername(username)
                .or(() -> userRepository.findByPersonalEmail(personalEmail));
        if (existingEmployeeOptional.isPresent()) {
            User existingEmployee = existingEmployeeOptional.get();
            if (!existingEmployee.isClient()) {
                existingEmployee = userFactory.assignClientAttributes(existingEmployee,
                        getRolesFromRoleNames(clientRoleNames), passwordHash, personalEmail);
                if (emailEngine.sendTemplatedEmail(new EmailMessage(personalEmail,
                        "Verify your MarketPilot account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                    if (pendingVerificationUserRepository.save(existingEmployee))
                        return RegistrationResult.PENDING_VERIFICATION;
            }
            else if (passwordHash.equals(existingEmployee.getClientPasswordHash()))
                    return RegistrationResult.ALREADY_REGISTERED;
        }

        return RegistrationResult.FAILURE;
    }

    public RegistrationResult initiateEmployeeRegistration(String employeeId, String username, char[] rawPassword,
                                 Set<Role.RoleName> employeeRoleNames, String employeeEmail,
                                 String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (!employeeRepository.employeeIdExists(employeeId))
            return RegistrationResult.FAILURE;

        Optional<User> userOptional = userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        if (userOptional.isPresent()) {
            if (passwordHash.equals(userOptional.get().getEmployeePasswordHash()))
                return RegistrationResult.ALREADY_REGISTERED;
            else
                return RegistrationResult.FAILURE;
        }

        User newUser = userFactory.createEmployeeUser(employeeId, getRolesFromRoleNames(employeeRoleNames),
                username, passwordHash, employeeEmail, firstName, middleName, lastName);

        try {
            if (emailEngine.sendTemplatedEmail(new EmailMessage(employeeEmail,
                    "Verify your MarketPilot employee account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                if (pendingVerificationUserRepository.save(newUser))
                    return RegistrationResult.PENDING_VERIFICATION;
        }
        catch (Exception e) {
            return RegistrationResult.FAILURE;
        }

        return RegistrationResult.FAILURE;
    }

    public RegistrationResult initiateEmployeeRegistrationForExistingClient(String employeeId, String username,
                                                                            char[] rawPassword,
                                                                            Set<Role.RoleName> employeeRoleNames,
                                                                            String employeeEmail) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (!employeeRepository.employeeIdExists(employeeId))
            return RegistrationResult.FAILURE;

        Optional<User> existingClientOptional = userRepository.findByEmployeeId(employeeId)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail));
        if (existingClientOptional.isPresent()) {
            User existingClient = existingClientOptional.get();
            if (!existingClient.isEmployee()) {
                existingClient = userFactory.assignEmployeeAttributes(existingClient, employeeId,
                        getRolesFromRoleNames(employeeRoleNames), passwordHash, employeeEmail);
                if (emailEngine.sendTemplatedEmail(new EmailMessage(employeeEmail,
                        "Verify your MarketPilot account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                    if (pendingVerificationUserRepository.save(existingClient))
                        return RegistrationResult.PENDING_VERIFICATION;
            }
            else {
                if (passwordHash.equals(existingClient.getEmployeePasswordHash()))
                    return RegistrationResult.ALREADY_REGISTERED;
                else
                    return RegistrationResult.FAILURE;
            }
        }

        return RegistrationResult.FAILURE;
    }

    //TODO: Service that runs in the background and removes users from the pending repo if verification period has 
    // expired
    public RegistrationResult completeRegistration(String username, UserType registrationUserType,
                                                   String verificationCodeAttempt) {
        if (registrationUserType == null)
            return RegistrationResult.FAILURE;

        Optional<User> userOptional = pendingVerificationUserRepository.findByUsername(username);
        if (userOptional.isEmpty())
            return RegistrationResult.FAILURE;

        User user = userOptional.get();
        UUID uuid;
        try {
            uuid = user.getUUID();
        } catch (IllegalStateException e) {
            return RegistrationResult.FAILURE;
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
                        return RegistrationResult.SUCCESS;
                    }
            }
            catch (Exception e) {
                return RegistrationResult.FAILURE;
            }
        }

        return RegistrationResult.FAILURE;
    }

    private Set<Role> getRolesFromRoleNames(Set<Role.RoleName> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : roleNames) {
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
            Role role = optionalRole.orElseThrow(() -> new NoSuchElementException(roleName + " role not found."));
            roles.add(role);
        }

        return roles;
    }
}
