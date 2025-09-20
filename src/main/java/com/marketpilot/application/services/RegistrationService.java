package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.services.UserFactory;

import java.util.*;

public class RegistrationService {
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
    public void registerClient(String username, char[] rawPassword,
                               Set<Role.RoleName> clientRoleNames, String personalEmail,
                               String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("username " + username + " is taken");
        if (userRepository.findByPersonalEmail(personalEmail).isPresent())
            throw new IllegalArgumentException("email " + personalEmail + " is already registered");

        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : clientRoleNames) {
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
            Role role = optionalRole.orElseThrow(() -> new NoSuchElementException(roleName + " role not found."));
            roles.add(role);
        }
        userRepository.save(userFactory.createClientUser(roles, username, passwordHash, personalEmail,
                firstName, middleName, lastName));
    }

    //TODO: Allow employee registration if already registered as a client (personal investor)
    //TODO: 2FA
    public void registerEmployee(String employeeId, String username, char[] rawPassword,
                                 Set<Role.RoleName> employeeRoleNames, String employeeEmail,
                                 String firstName, String middleName, String lastName) {
        String passwordHash = passwordHasher.hash(rawPassword);
        Arrays.fill(rawPassword, '\0');
        rawPassword = null;

        if (userRepository.findByEmployeeId(employeeId).isPresent())
            throw new IllegalArgumentException("employeeId " + employeeId + " is already registered");
        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("username " + username + " is taken");
        if (userRepository.findByEmployeeEmail(employeeEmail).isPresent())
            throw new IllegalArgumentException("email " + employeeEmail + " is already registered");

        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : employeeRoleNames) {
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
            Role role = optionalRole.orElseThrow(() -> new NoSuchElementException(roleName + " role not found."));
            roles.add(role);
        }
        userRepository.save(userFactory.createEmployeeUser(employeeId, roles, username, passwordHash,
                employeeEmail, firstName, middleName, lastName));
    }
}
