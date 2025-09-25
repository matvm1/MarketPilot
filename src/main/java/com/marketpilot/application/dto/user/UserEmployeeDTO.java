package com.marketpilot.application.dto.user;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Set;

public class UserEmployeeDTO extends UserAbstractDTO {
    private final String employeeId;
    private final String email;

    public UserEmployeeDTO(String employeeId, String username, Set<Role> roles, String email, String firstName, String middleName, String lastName) {
        super(username, roles, firstName, middleName, lastName);
        this.employeeId = employeeId;
        this.email = email;
    }

    public String getEmployeeId() { return employeeId; }

    public String getEmail() { return email; }
}