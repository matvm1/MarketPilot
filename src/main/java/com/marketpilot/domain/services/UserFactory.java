package com.marketpilot.domain.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserRoleAssignment;

import java.util.HashSet;
import java.util.Set;

public class UserFactory {
    public User createUser(String employeeId, Set<Role> rolesToAssign, String username, String passwordHash,
                           String personalEmail, String employeeEmail, String firstName, String middleName, String lastName) {
        User newUser = new User(employeeId, username, personalEmail, employeeEmail, firstName, middleName, lastName);
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        for (Role role : rolesToAssign)
            userRoleAssignments.add(new UserRoleAssignment(newUser, role));
        newUser.grantRoles(userRoleAssignments);
        newUser.setPasswordHash(passwordHash);
        return newUser;
    }
}
