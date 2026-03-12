package com.marketpilot.domain.entities.auth;

import com.marketpilot.domain.entities.PersistentEntity;
import com.marketpilot.domain.entities.auth.profile.ClientProfile;
import com.marketpilot.domain.entities.auth.profile.EmployeeProfile;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.marketpilot.util.EqualityUtil.noneNull;
import static com.marketpilot.util.EqualityUtil.allNull;

@Entity
@Table(
        name = "APP_USER"
)
public class User extends PersistentEntity {
    @Column(unique = true, updatable = false, nullable = false)
    private UUID uuid;

    @ManyToMany
    @JoinTable(
            name = "APP_USER_ROLE",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Column(nullable = false, updatable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Transient private ClientProfile clientProfile;
    @Transient private EmployeeProfile employeeProfile;

    public User(String username, String firstName, String middleName, String lastName, ClientProfile clientProfile, EmployeeProfile employeeProfile) {
        if (allNull(clientProfile, employeeProfile))
            throw new IllegalArgumentException("at least one profile must be provided");
        if (username == null)
            throw new IllegalArgumentException("username cannot be null");
        if (username.isBlank())
            throw new IllegalArgumentException("username cannot be blank");
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("lastName cannot be empty");

        this.username = username;
        this.roles = new HashSet<>();
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;

        this.clientProfile = clientProfile;
        this.employeeProfile = employeeProfile;
    }

    public UUID getUUID() {
        if (this.uuid == null)
            throw new IllegalStateException("uuid is null. UserFactory User creation methods should set the UUID of User objects to valid UUIDs");

        return uuid;
    }

    public String getUsername() { return username; }

    public Set<Role> getRoles() { return roles; }
    
    public Role getRole(Role.RoleName roleName) {
        Role roleResult = null;
        for (Role role : roles)
            if (role.getRoleName().equals(roleName))
                roleResult = role;
        return roleResult;
    }

    public boolean hasRole(Role.RoleName roleName) {
        for (Role role : roles)
            if (role.getRoleName().equals(roleName))
                return true;

        return false;
    }

    public String getFirstName() { return firstName; }

    public String getMiddleName() { return middleName; }

    public String getLastName() { return lastName; }

    public String getFullName() { return firstName + " " + (middleName == null ? "" : middleName) + " " + lastName; }

    public boolean isClient() {
        if (clientProfile == null && employeeProfile == null)
            throw new IllegalStateException("user must be a client and/or employee prior to accessing isClient()");
        return clientProfile != null;
    }

    public boolean isEmployee() {
        if (clientProfile == null && employeeProfile == null)
            throw new IllegalStateException("user must be a client and/or employee prior to accessing isEmployee()");
        return employeeProfile != null;
    }

    public void setUUID(UUID uuid) {
        final int RANDOM_UUID_GENERATION_VERSION_NUMBER = 4;
        if (uuid == null)
            throw new IllegalArgumentException("uuid cannot be null");
        if (uuid.version() != RANDOM_UUID_GENERATION_VERSION_NUMBER)
            throw new IllegalArgumentException("uuid version must be " + RANDOM_UUID_GENERATION_VERSION_NUMBER);

        this.uuid = uuid;
    }

    public void grantRole(Role role) {
        if (role == null)
            throw new IllegalArgumentException("role cannot be null");

        this.roles.add(role);
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

    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    public EmployeeProfile getEmployeeProfile() {
        return employeeProfile;
    }

    public void setClientProfile(ClientProfile clientProfile) {
        this.clientProfile = clientProfile;
    }

    public void setEmployeeProfile(EmployeeProfile employeeProfile) {
        this.employeeProfile = employeeProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof User))
            return false;

        if (!(this.getRoles().equals(((User) o).getRoles())))
            return false;

        return this.uuid.equals(((User) o).uuid) &&
                this.username.equals(((User) o).username) &&
                this.firstName.equals(((User) o).firstName) &&
                this.middleName.equals(((User)o).middleName) &&
                this.lastName.equals(((User)o).lastName) &&
                (
                        (noneNull(this.clientProfile, ((User) o).clientProfile) && this.clientProfile.equals(((User) o).clientProfile)) ||
                        allNull(this.clientProfile, ((User) o).clientProfile)
                ) &&
                (
                        (noneNull(this.employeeProfile, ((User) o).employeeProfile) && this.employeeProfile.equals(((User) o).employeeProfile)) ||
                        allNull(this.employeeProfile, ((User) o).employeeProfile)
                );
    }
}
