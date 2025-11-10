package com.marketpilot.application.dto.auth;

import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationContextTest {
    @Test
    void constructor_throwsForNullPrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
                        new AuthenticationContext(null, TestRoles.PERSONAL_INVESTOR_ROLE));
    }

    @Test
    void constructor_throwsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new AuthenticationContext(new User("ab123456", "johnmdoe", "johnmdoe@outlook.com",
                        "johnmdoe@company.com","John", "M", "Doe"), null));
    }
}
