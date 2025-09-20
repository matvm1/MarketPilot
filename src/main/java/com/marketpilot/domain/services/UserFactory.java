package com.marketpilot.domain.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import java.util.Set;

public class UserFactory {
    public User createClientUser(Set<Role> clientRoles, String username, String clientPasswordHash,
                                 String personalEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(null, username, personalEmail, null, firstName, middleName, lastName);
        validateRolesAndGrant(newUser, clientRoles, Role.RoleType.CLIENT);
        newUser.setClientPasswordHash(clientPasswordHash);

        return newUser;
    }

    public User createEmployeeUser(String employeeId, Set<Role> employeeRoles, String username, String employeePasswordHash,
                                 String employeeEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(employeeId, username, null, employeeEmail, firstName, middleName, lastName);
        validateRolesAndGrant(newUser, employeeRoles, Role.RoleType.EMPLOYEE);
        newUser.setEmployeePasswordHash(employeePasswordHash);

        return newUser;
    }

    public User assignEmployeeAttributes(User existingClient, String employeeId, Set<Role> employeeRoles,
                                                  String employeePasswordHash, String employeeEmail) {
        if (existingClient == null)
            throw new IllegalArgumentException("existingClient cannot be null");
        if (employeeRoles == null)
            throw new IllegalArgumentException("employeeRoles cannot be null");
        if (employeeRoles.isEmpty())
            throw new IllegalArgumentException("employeeRoles cannot be empty");

        existingClient.setEmployeeId(employeeId);
        validateRolesAndGrant(existingClient, employeeRoles, Role.RoleType.EMPLOYEE);
        existingClient.setEmployeePasswordHash(employeePasswordHash);
        existingClient.setEmployeeEmail(employeeEmail);

        return existingClient;
    }

    public User assignClientAttributes(User existingEmployee, Set<Role> clientRoles,
                                         String clientPasswordHash, String personalEmail) {
        if (existingEmployee == null)
            throw new IllegalArgumentException("existingClient cannot be null");
        if (clientRoles == null)
            throw new IllegalArgumentException("clientRoles cannot be null");
        if (clientRoles.isEmpty())
            throw new IllegalArgumentException("clientRoles cannot be empty");

        validateRolesAndGrant(existingEmployee, clientRoles, Role.RoleType.CLIENT);
        existingEmployee.setClientPasswordHash(clientPasswordHash);
        existingEmployee.setPersonalEmail(personalEmail);

        return existingEmployee;
    }

    public void validateRolesAndGrant(User user, Set<Role> roles, Role.RoleType expectedRoleType) {
        // validate all roles prior to granting them
        for (Role role : roles)
            if (role.getRoleType() != expectedRoleType)
                throw new IllegalArgumentException("Role " + role.getRoleName() + " is not of expected roleType " + expectedRoleType);
        for (Role role : roles)
            user.grantRole(role);
    }
}
