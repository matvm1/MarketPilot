package com.marketpilot.application.services;

import com.marketpilot.application.dto.user.UserClientDTO;
import com.marketpilot.application.dto.user.UserEmployeeDTO;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.Set;
import java.util.UUID;

public class UserFactory {
    public User createClientUser(Set<Role> clientRoles, String username, String personalEmail,
                                 String firstName, String middleName, String lastName) {
        User newUser = new User(null, username, personalEmail, null, firstName, middleName, lastName);
        validateRolesAndGrant(newUser, clientRoles, UserType.CLIENT);
        newUser.setUUID(UUID.randomUUID());
        newUser.setClient(true);

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
        User newUser = new User(employeeId, username, null, employeeEmail, firstName, middleName, lastName);
        validateRolesAndGrant(newUser, employeeRoles, UserType.EMPLOYEE);
        newUser.setUUID(UUID.randomUUID());
        newUser.setEmployee(true);

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

        existingClient.setEmployeeId(employeeId);
        validateRolesAndGrant(existingClient, employeeRoles, UserType.EMPLOYEE);
        existingClient.setEmployeeEmail(employeeEmail);
        existingClient.setEmployee(true);

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
        existingEmployee.setPersonalEmail(personalEmail);
        existingEmployee.setClient(true);

        return existingEmployee;
    }

    public User assignClientAttributes(User existingEmployee, UserClientDTO userClientDTO) {
        if (userClientDTO == null)
            throw new IllegalArgumentException("userClientDTO cannot be null");

        return assignClientAttributes(existingEmployee,
                userClientDTO.getRoles(),
                userClientDTO.getEmail());
    }

    // use only when populating a User object from persisted data
    public User hydrate(UUID uuid, String employeeId, String username, String personalEmail, String employeeEmail,
                         String firstName, String middleName, String lastName, boolean isClient, boolean isEmployee) {
        User user = new User(employeeId, username, personalEmail, employeeEmail, firstName, middleName, lastName);

        user.setUUID(uuid);
        user.setClient(isClient);
        user.setEmployee(isEmployee);

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
