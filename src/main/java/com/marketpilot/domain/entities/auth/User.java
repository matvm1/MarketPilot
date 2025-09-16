package com.marketpilot.domain.entities.auth;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Set;

public class User {
    private final int id;
    private Set<UserRoleAssignment> userRoleAssignments;

    private String username;
    private String firstName;
    private String middleName;
    private String lastName;

    public User(int id, String employeeId, String username, String personalEmail, String employeeEmail,
                String firstName, String middleName,
                String lastName) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (id <= 0)
            throw new IllegalArgumentException("id cannot be non-positive");
        if (employeeId == null && employeeEmail != null)
            throw new IllegalArgumentException("employeeId cannot be null when employeeEmail is not null");
        if (employeeId != null && employeeEmail == null)
            throw new IllegalArgumentException("employeeEmail cannot be null when employeeId is not null");
        if (employeeId != null && employeeId.isBlank())
            throw new IllegalArgumentException("employeeId cannot be blank");
        if (username == null)
            throw new IllegalArgumentException("username cannot be null");
        if (username.isBlank())
            throw new IllegalArgumentException("username cannot be blank");
        if (personalEmail == null && employeeEmail == null)
            throw new IllegalArgumentException("personalEmail and employeeEmail cannot both be null");
        if (personalEmail != null && personalEmail.isBlank())
            throw new IllegalArgumentException("personalEmail cannot be blank");
        if (employeeEmail != null && employeeEmail.isBlank())
            throw new IllegalArgumentException("employeeEmail cannot be blank");
        if (personalEmail != null && !emailValidator.isValid(personalEmail))
            throw new IllegalArgumentException("personalEmail '" + personalEmail + "' is not in a valid email format");
        if (employeeEmail != null && !emailValidator.isValid(employeeEmail))
            throw new IllegalArgumentException("employeeEmail '" + employeeEmail + "' is not in a valid email format");
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("lastName cannot be empty");

        this.username = username;
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public void grantRoles(Set<UserRoleAssignment> userRoleAssignments) {
        if (userRoleAssignments == null)
            throw new IllegalArgumentException("userRoleAssignments cannot be null");
        if (userRoleAssignments.isEmpty())
            throw new IllegalArgumentException("userRoleAssignment cannot be empty");

        this.userRoleAssignments = userRoleAssignments;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + middleName + " " + lastName;
    }

    public Set<UserRoleAssignment> getUserRoleAssignments() {
        return userRoleAssignments;
    }
}
