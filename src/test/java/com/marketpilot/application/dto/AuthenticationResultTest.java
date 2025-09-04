package com.marketpilot.application.dto;

import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationResultTest {
    @Test
    void constructor_throwsForNullPrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
                        new AuthenticationResult(null, TestRoles.PERSONAL_INVESTOR_ROLE));
    }

    @Test
    void constructor_throwsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new AuthenticationResult(new User(1, "johnmdoe", "John", "M", "Doe"), null));
    }
}
