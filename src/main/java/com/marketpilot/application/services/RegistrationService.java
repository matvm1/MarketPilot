package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.services.UserFactory;

import java.util.*;

public class RegistrationService {
    public enum RegistrationResult {
        SUCCESS,
        FAILURE,
        ALREADY_REGISTERED
    }

    private final PasswordHasher passwordHasher;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserFactory userFactory;

    public RegistrationService(PasswordHasher passwordHasher, UserRepository userRepository, RoleRepository roleRepository, UserFactory userFactory) {
        this.passwordHasher = passwordHasher;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userFactory = userFactory;
    }

    //TODO: Allow client registration if already registered as an employee
    //TODO: 2FA
    public RegistrationResult registerClient(String username, char[] rawPassword,
                               Set<Role.RoleName> clientRoleNames, String personalEmail,
                               String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByUsername(username)
                .or(() -> userRepository.findByPersonalEmail(personalEmail)).isPresent())
            return RegistrationResult.ALREADY_REGISTERED;

        userRepository.save(userFactory.createClientUser(getRolesFromRoleNames(clientRoleNames), username,
                passwordHash, personalEmail, firstName, middleName, lastName));

        return RegistrationResult.SUCCESS;
    }

    //TODO: Allow employee registration if already registered as a client (personal investor)
    //TODO: 2FA
    public RegistrationResult registerEmployee(String employeeId, String username, char[] rawPassword,
                                 Set<Role.RoleName> employeeRoleNames, String employeeEmail,
                                 String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmployeeId(employeeId))
                .or(() -> userRepository.findByEmployeeEmail(employeeEmail)).isPresent())
            return RegistrationResult.ALREADY_REGISTERED;

        userRepository.save(userFactory.createEmployeeUser(employeeId, getRolesFromRoleNames(employeeRoleNames),
                username, passwordHash, employeeEmail, firstName, middleName, lastName));

        return RegistrationResult.SUCCESS;
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
