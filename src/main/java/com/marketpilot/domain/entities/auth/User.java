package com.marketpilot.domain.entities.auth;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {
    private UUID uuid;
    private String employeeId;
    private Set<UserRoleAssignment> userRoleAssignments;
    private final String username;
    private String clientPasswordHash;
    private String employeePasswordHash;
    private String personalEmail;
    private String employeeEmail;
    private String firstName;
    private String middleName;
    private String lastName;

    public User(String employeeId, String username, String personalEmail, String employeeEmail,
                String firstName, String middleName, String lastName) {
        EmailValidator emailValidator = EmailValidator.getInstance();
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

        this.employeeId = employeeId;
        this.username = username;
        this.userRoleAssignments = new HashSet<>();
        this.personalEmail = personalEmail;
        this.employeeEmail = employeeEmail;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public UUID getUUID() {
        if (this.uuid == null)
            throw new IllegalStateException("uuid is null. UserFactory User creation methods should set the UUID of User objects to valid UUIDs");

        return uuid;
    }

    public String getEmployeeId() { return employeeId; }

    public String getUsername() { return username; }

    public String getClientPasswordHash() { return clientPasswordHash; }

    public String getEmployeePasswordHash() { return employeePasswordHash; }

    public Set<UserRoleAssignment> getUserRoleAssignments() { return userRoleAssignments; }

    public String getPersonalEmail() { return personalEmail; }

    public String getEmployeeEmail() { return employeeEmail; }

    public String getFirstName() { return firstName; }

    public String getMiddleName() { return middleName; }

    public String getLastName() { return lastName; }

    public String getFullName() { return firstName + " " + middleName + " " + lastName; }

    public void setUUID(UUID uuid) {
        final int RANDOM_UUID_GENERATION_VERSION_NUMBER = 4;
        if (uuid == null)
            throw new IllegalArgumentException("uuid cannot be null");
        if (uuid.version() != RANDOM_UUID_GENERATION_VERSION_NUMBER)
            throw new IllegalArgumentException("uuid version must be " + RANDOM_UUID_GENERATION_VERSION_NUMBER);

        this.uuid = uuid;
    }

    public void setEmployeeId(String employeeId) {
        if (employeeId == null)
            throw new IllegalArgumentException("employeeId cannot be null");
        if (employeeId.isBlank())
            throw new IllegalArgumentException("employeeId cannot be blank");

        this.employeeId = employeeId;
    }

    public void setClientPasswordHash(String clientPasswordHash) {
        if (clientPasswordHash == null)
            throw new IllegalArgumentException("clientPasswordHash cannot be null");
        if (clientPasswordHash.isBlank())
            throw new IllegalArgumentException("clientPasswordHash cannot be blank");
        this.clientPasswordHash = clientPasswordHash;
    }

    public void setEmployeePasswordHash(String employeePasswordHash) {
        if (employeePasswordHash == null)
            throw new IllegalArgumentException("employeePasswordHash cannot be null");
        if (employeePasswordHash.isBlank())
            throw new IllegalArgumentException("employeePasswordHash cannot be blank");
        this.employeePasswordHash = employeePasswordHash;
    }

    public void grantRole(Role role) {
        if (role == null)
            throw new IllegalArgumentException("role cannot be null");

        this.userRoleAssignments.add(new UserRoleAssignment(this, role));
    }

    public void setPersonalEmail(String personalEmail) {
        if (personalEmail == null)
            throw new IllegalArgumentException("personalEmail cannot be null");
        if (personalEmail.isBlank())
            throw new IllegalArgumentException("personalEmail cannot be blank");
        this.personalEmail = personalEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        if (employeeEmail == null)
            throw new IllegalArgumentException("employeeEmail cannot be null");
        if (employeeEmail.isBlank())
            throw new IllegalArgumentException("employeeEmail cannot be blank");
        this.employeeEmail = employeeEmail;
    }

    public void setFirstName(String firstName) {
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof User))
            return false;

        Set<Role> thisRoles = new HashSet<>();
        for (UserRoleAssignment userRoleAssignment : userRoleAssignments)
            thisRoles.add(userRoleAssignment.getRole());
        Set<Role> oRoles = new HashSet<>();
        for (UserRoleAssignment userRoleAssignment : ((User) o).userRoleAssignments)
            oRoles.add(userRoleAssignment.getRole());
        if (!(thisRoles.equals(oRoles)))
            return false;

        return this.username.equals(((User) o).username) &&
                this.employeeId.equals(((User) o).employeeId) &&
                this.personalEmail.equals(((User) o).personalEmail) &&
                this.employeeEmail.equals(((User) o).employeeEmail) &&
                this.firstName.equals(((User) o).firstName) &&
                this.middleName.equals(((User)o).middleName) &&
                this.lastName.equals(((User)o).lastName);
    }
}
