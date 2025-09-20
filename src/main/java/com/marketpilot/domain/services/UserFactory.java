package com.marketpilot.domain.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserRoleAssignment;

import java.util.HashSet;
import java.util.Set;

public class UserFactory {
    //TODO: Integrate employee registration with an existing client and vice-versa
    public User createClientUser(Set<Role> rolesToAssign, String username, String clientPasswordHash,
                                 String personalEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(null, username, personalEmail, null, firstName, middleName, lastName);
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        for (Role role : rolesToAssign)
            userRoleAssignments.add(new UserRoleAssignment(newUser, role));
        newUser.grantRoles(userRoleAssignments);
        newUser.setClientPasswordHash(clientPasswordHash);
        return newUser;
    }

    public User createEmployeeUser(String employeeId, Set<Role> rolesToAssign, String username, String employeePasswordHash,
                                 String employeeEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(employeeId, username, null, employeeEmail, firstName, middleName, lastName);
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        for (Role role : rolesToAssign)
            userRoleAssignments.add(new UserRoleAssignment(newUser, role));
        newUser.grantRoles(userRoleAssignments);
        newUser.setEmployeePasswordHash(employeePasswordHash);
        return newUser;
    }
}
