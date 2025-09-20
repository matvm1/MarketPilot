package com.marketpilot.domain.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserRoleAssignment;

import java.util.HashSet;
import java.util.Set;

public class UserFactory {
    //TODO: Integrate employee registration with an existing client and vice-versa
    public User createClientUser(Set<Role> clientRoles, String username, String clientPasswordHash,
                                 String personalEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(null, username, personalEmail, null, firstName, middleName, lastName);

        // validate all roles prior to granting them
        for (Role clientRole : clientRoles)
            if (clientRole.getRoleType() != Role.RoleType.CLIENT)
                throw new IllegalArgumentException("clientRoles cannot contain non-client roles");
        for (Role clientRole : clientRoles)
            newUser.grantRole(clientRole);

        newUser.setClientPasswordHash(clientPasswordHash);
        return newUser;
    }

    public User createEmployeeUser(String employeeId, Set<Role> employeeRoles, String username, String employeePasswordHash,
                                 String employeeEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(employeeId, username, null, employeeEmail, firstName, middleName, lastName);

        // validate all roles prior to granting them
        for (Role employeeRole : employeeRoles)
            if (employeeRole.getRoleType() != Role.RoleType.EMPLOYEE)
                throw new IllegalArgumentException("employeeRoles cannot contain non-employee roles");
        for (Role employeeRole : employeeRoles)
            newUser.grantRole(employeeRole);

        newUser.setEmployeePasswordHash(employeePasswordHash);
        return newUser;
    }
}
