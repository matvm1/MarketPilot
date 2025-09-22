package com.marketpilot.application.services;

import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.persistence.PendingVerificationUserRepository;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
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
    private final RoleRepository roleRepository;
    private final EmailEngine emailEngine;
    private final PasswordHasher passwordHasher;
    private final UserFactory userFactory;
    private final String VERIFICATION_EMAIL_TEMPLATE = "verification_email.html";

    public RegistrationService(UserRepository userRepository,
                               PendingVerificationUserRepository pendingVerificationUserRepository,
                               RoleRepository roleRepository,
                               EmailEngine emailEngine, PasswordHasher passwordHasher, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.pendingVerificationUserRepository = pendingVerificationUserRepository;
        this.roleRepository = roleRepository;
        this.emailEngine = emailEngine;
        this.passwordHasher = passwordHasher;
        this.userFactory = userFactory;
    }

    //TODO: Allow client registration if already registered as an employee
    public RegistrationResult initiateClientRegistration(String username, char[] rawPassword,
                               Set<Role.RoleName> clientRoleNames, String personalEmail,
                               String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByUsername(username)
                .or(() -> userRepository.findByPersonalEmail(personalEmail)).isPresent())
            return RegistrationResult.ALREADY_REGISTERED;

        User newUser = userFactory.createClientUser(getRolesFromRoleNames(clientRoleNames), username,
                passwordHash, personalEmail, firstName, middleName, lastName);
        try {
            if (emailEngine.sendTemplatedEmail(new EmailMessage(personalEmail,
                    "Verify your MarketPilot account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                pendingVerificationUserRepository.save(newUser);
            else
                return RegistrationResult.FAILURE;
        }
        catch (Exception e) {
            return RegistrationResult.FAILURE;
        }

        return RegistrationResult.PENDING_VERIFICATION;
    }

    //TODO: Allow employee registration if already registered as a client (personal investor)
    public RegistrationResult initiateEmployeeRegistration(String employeeId, String username, char[] rawPassword,
                                 Set<Role.RoleName> employeeRoleNames, String employeeEmail,
                                 String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmployeeId(employeeId))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail)).isPresent())
            return RegistrationResult.ALREADY_REGISTERED;

        User newUser = userFactory.createEmployeeUser(employeeId, getRolesFromRoleNames(employeeRoleNames),
                username, passwordHash, employeeEmail, firstName, middleName, lastName);

        try {
            if (emailEngine.sendTemplatedEmail(new EmailMessage(employeeEmail,
                    "Verify your MarketPilot employee account", null, null), VERIFICATION_EMAIL_TEMPLATE))
                pendingVerificationUserRepository.save(newUser);
            else
                return  RegistrationResult.FAILURE;
        }
        catch (Exception e) {
            return RegistrationResult.FAILURE;
        }

        return RegistrationResult.PENDING_VERIFICATION;
    }

    //TODO: Service that runs in the background and removes users from the pending repo if verification period has
    // expired
    //TODO: unit tests
    //TODO: Separate client and employee verification codes
    public RegistrationResult completeRegistration(User user, String verificationCodeAttempt) {
        String username = user.getUsername();
        Optional<String> verificationCode = pendingVerificationUserRepository.getVerificationCode(username);
        if (verificationCode.isPresent() && verificationCode.get().equals(verificationCodeAttempt)) {
            try {
                if (pendingVerificationUserRepository.deleteByUsername(username))
                    userRepository.save(user);
                else
                    return RegistrationResult.FAILURE;
            }
            catch (Exception e) {
                return RegistrationResult.FAILURE;
            }
            return RegistrationResult.SUCCESS;
        }
        else
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
