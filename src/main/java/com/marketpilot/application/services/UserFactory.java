package com.marketpilot.application.services;

import com.marketpilot.application.dto.user.UserClientDTO;
import com.marketpilot.application.dto.user.UserEmployeeDTO;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.entities.auth.profile.ClientProfile;
import com.marketpilot.domain.entities.auth.profile.EmployeeProfile;

import java.util.Set;
import java.util.UUID;

public class UserFactory {
    public User createClientUser(Set<Role> clientRoles, String username, String personalEmail,
                                 String firstName, String middleName, String lastName) {
        User newUser = new User(username, firstName, middleName, lastName, new ClientProfile(personalEmail), null);
        validateRolesAndGrant(newUser, clientRoles, UserType.CLIENT);
        newUser.setUUID(UUID.randomUUID());

        return newUser;
    }

    public User createClientUser(UserClientDTO userClientDTO) {
        if (userClientDTO == null)
            throw new IllegalArgumentException("userClientDTO cannot be null");

        return createClientUser(userClientDTO.getRoles(),
                userClientDTO.getUsername(),
                userClientDTO.getEmail(),
                userClientDTO.getFirstName(),
                userClientDTO.getMiddleName(),
                userClientDTO.getLastName());
    }

    public User createEmployeeUser(String employeeId, Set<Role> employeeRoles, String username, String employeeEmail,
                                   String firstName, String middleName, String lastName) {
        User newUser = new User(username, firstName, middleName, lastName, null, new EmployeeProfile(employeeId, employeeEmail));
        validateRolesAndGrant(newUser, employeeRoles, UserType.EMPLOYEE);
        newUser.setUUID(UUID.randomUUID());

        return newUser;
    }

    public User createEmployeeUser(UserEmployeeDTO userEmployeeDTO) {
        if (userEmployeeDTO == null)
            throw new IllegalArgumentException("userEmployeeDTO cannot be null");

        return createEmployeeUser(userEmployeeDTO.getEmployeeId(),
                userEmployeeDTO.getRoles(),
                userEmployeeDTO.getUsername(),
                userEmployeeDTO.getEmail(),
                userEmployeeDTO.getFirstName(),
                userEmployeeDTO.getMiddleName(),
                userEmployeeDTO.getLastName());
    }

    public User assignEmployeeAttributes(User existingClient, String employeeId, Set<Role> employeeRoles, String employeeEmail) {
        if (existingClient == null)
            throw new IllegalArgumentException("existingClient cannot be null");
        if (employeeRoles == null)
            throw new IllegalArgumentException("employeeRoles cannot be null");
        if (employeeRoles.isEmpty())
            throw new IllegalArgumentException("employeeRoles cannot be empty");

        existingClient.setEmployeeProfile(new EmployeeProfile(employeeId, employeeEmail));
        validateRolesAndGrant(existingClient, employeeRoles, UserType.EMPLOYEE);

        return existingClient;
    }

    public User assignEmployeeAttributes(User existingClient, UserEmployeeDTO userEmployeeDTO) {
        if (userEmployeeDTO == null)
            throw new IllegalArgumentException("userEmployeeDTO cannot be null");

        return assignEmployeeAttributes(existingClient,
                userEmployeeDTO.getEmployeeId(),
                userEmployeeDTO.getRoles(),
                userEmployeeDTO.getEmail());
    }

    public User assignClientAttributes(User existingEmployee, Set<Role> clientRoles, String personalEmail) {
        if (existingEmployee == null)
            throw new IllegalArgumentException("existingClient cannot be null");
        if (clientRoles == null)
            throw new IllegalArgumentException("clientRoles cannot be null");
        if (clientRoles.isEmpty())
            throw new IllegalArgumentException("clientRoles cannot be empty");

        validateRolesAndGrant(existingEmployee, clientRoles, UserType.CLIENT);
        existingEmployee.setClientProfile(new ClientProfile(personalEmail));

        return existingEmployee;
    }

    public User assignClientAttributes(User existingEmployee, UserClientDTO userClientDTO) {
        if (userClientDTO == null)
            throw new IllegalArgumentException("userClientDTO cannot be null");

        return assignClientAttributes(existingEmployee,
                userClientDTO.getRoles(),
                userClientDTO.getEmail());
    }

    // TODO: Review if still needed after JPA refactor
    // If keeping, add fromArgs() static method to profile classes which return null if the created profile is in an invalid state
    // use only when populating a User object from persisted data
    // TODO: Remove isClient is Employee
    public User hydrate(UUID uuid, String employeeId, String username, String personalEmail, String employeeEmail,
                         String firstName, String middleName, String lastName, boolean isClient, boolean isEmployee) {
        ClientProfile clientProfile = null;
        if (personalEmail != null)
            clientProfile = new ClientProfile(personalEmail);

        EmployeeProfile employeeProfile = null;
        if (employeeId != null && employeeEmail != null)
            employeeProfile = new EmployeeProfile(employeeId, employeeEmail);

        User user = new User(username, firstName, middleName, lastName, clientProfile, employeeProfile);
        user.setUUID(uuid);

        return user;
    }

    // use only when populating a User object from persisted data
    public void hydrateWithRoles(User user, Set<Role> roles) {
        if (user == null)
            throw new IllegalArgumentException("user cannot be null");
        if (roles == null)
            throw new IllegalArgumentException("roles cannot be null");
        if (roles.isEmpty())
            throw new IllegalArgumentException("roles cannot be empty");

        validateRolesAndGrant(user, roles, null);
    }

    // ignores expectedUserType if expectedUserType is null
    private void validateRolesAndGrant(User user, Set<Role> roles, UserType expectedUserType) {
        if (roles == null)
            throw new IllegalArgumentException("roles cannot be null");
        if (roles.isEmpty())
            throw new IllegalArgumentException("roles cannot be empty");
        // validate all roles prior to granting them
        if (expectedUserType != null)
            for (Role role : roles)
                if (role.getUserType() != expectedUserType)
                    throw new IllegalArgumentException("Role " + role.getRoleName() + " is not of expected roleType " + expectedUserType);
        for (Role role : roles)
            user.grantRole(role);
    }
}
