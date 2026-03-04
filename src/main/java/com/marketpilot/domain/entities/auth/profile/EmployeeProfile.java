package com.marketpilot.domain.entities.auth.profile;

import com.marketpilot.domain.entities.auth.User;
import org.apache.commons.validator.routines.EmailValidator;

public class EmployeeProfile {
    private String employeeId;
    private String email;

    public EmployeeProfile(String employeeId, String email) {
        if (employeeId == null)
            throw new IllegalArgumentException("employeeId cannot be null");
        if (email == null)
            throw new IllegalArgumentException("email cannot be null");
        if (employeeId.isBlank())
            throw new IllegalArgumentException("employeeId cannot be blank");
        if (email.isBlank())
            throw new IllegalArgumentException("email cannot be blank");
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email))
            throw new IllegalArgumentException("email '" + email + "' is not in a valid email format");

        this.employeeId = employeeId;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null)
            throw new IllegalArgumentException("email cannot be null");
        if (email.isBlank())
            throw new IllegalArgumentException("email cannot be blank");

        this.email = email;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        if (employeeId == null)
            throw new IllegalArgumentException("employeeId cannot be null");
        if (employeeId.isBlank())
            throw new IllegalArgumentException("employeeId cannot be blank");

        this.employeeId = employeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof EmployeeProfile))
            return false;

        return this.employeeId.equals(((EmployeeProfile) o).employeeId) &&
                this.email.equals(((EmployeeProfile) o).email);
    }
}
