package com.marketpilot.application.services;

import com.marketpilot.application.ports.RoleRepository;
import com.marketpilot.application.ports.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.services.UserFactory;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class RegistrationService {
    private final UserFactory userFactory;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public RegistrationService(UserFactory userFactory, UserRepository userRepository, RoleRepository roleRepository) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void registerPersonalInvestor(String username, String personalEmail,
                                                String firstName, String middleName, String lastName) {
        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("username " + username + " is taken");
        if (userRepository.findByPersonalEmail(personalEmail).isPresent())
            throw new IllegalArgumentException("email " + personalEmail + " is already registered");

        Set<Role> roles = new HashSet<>();
        Optional<Role> optionalRole = roleRepository.findByRoleName(Role.RoleName.PersonalInvestor);
        Role personalInvestorRole = optionalRole.orElseThrow(() -> new NoSuchElementException("Personal investor role not found."));
        roles.add(personalInvestorRole);
        userRepository.save(userFactory.createUser(null, roles, username, personalEmail,
                null, firstName, middleName, lastName));
    }

    public void registerEmployee(Set<Role.RoleName> roleNames, String employeeId, String username, String employeeEmail,
                                 String firstName, String middleName, String lastName) {
        if (userRepository.findByEmployeeId(employeeId).isPresent())
            throw new IllegalArgumentException("employeeId " + employeeId + " is already registered");
        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalArgumentException("username " + username + " is taken");
        if (userRepository.findByEmployeeEmail(employeeEmail).isPresent())
            throw new IllegalArgumentException("email " + employeeEmail + " is already registered");

        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : roleNames) {
            if (roleName == Role.RoleName.PersonalInvestor)
                throw new IllegalArgumentException("The employee registration flow cannot register the user under the" +
                        " Personal Investor role");
            Optional<Role> optionalRole = roleRepository.findByRoleName(roleName);
            Role role = optionalRole.orElseThrow(() -> new NoSuchElementException(roleName + " role not found."));
            roles.add(role);
        }
        userRepository.save(userFactory.createUser(employeeId, roles, username, null,
                employeeEmail, firstName, middleName, lastName));
    }
}
