package com.marketpilot.domain.entities.auth.profile;

import com.marketpilot.domain.entities.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.validator.routines.EmailValidator;

@Entity
@Table(name = "CLIENT_PROFILE")
public class ClientProfile extends PersistentEntity {
    public ClientProfile() {}

    @Column(nullable = false, unique = true)
    private String email;

    public ClientProfile(String email) {
        if (email == null)
            throw new IllegalArgumentException("email cannot be null");
        if (email.isBlank())
            throw new IllegalArgumentException("email cannot be blank");
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email))
            throw new IllegalArgumentException("email '" + email + "' is not in a valid email format");

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
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email))
            throw new IllegalStateException("email " + email + " is not a valid email");

        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof ClientProfile))
            return false;

        return this.email.equals(((ClientProfile) o).email);
    }
}
