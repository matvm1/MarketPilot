package domain.services;

import domain.entities.auth.Role;
import domain.entities.auth.User;
import domain.entities.auth.UserRoleAssignment;

import java.util.HashSet;
import java.util.Set;

public class UserFactory {
    public User createUser(int id, Set<Role> rolesToAssign, String firstName, String middleName,
                           String lastName) {
        User newUser = new User(id, firstName, middleName, lastName);
        Set<UserRoleAssignment> userRoleAssignments = new HashSet<>();
        for (Role role : rolesToAssign)
            userRoleAssignments.add(new UserRoleAssignment(newUser, role));
        newUser.grantRoles(userRoleAssignments);
        return newUser;
    }
}
