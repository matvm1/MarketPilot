package com.marketpilot.application.dto;

import com.marketpilot.application.dto.auth.AuthenticationResult;
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
                new AuthenticationResult(new User("ab123456", "johnmdoe", "johnmdoe@outlook.com",
                        "johnmdoe@company.com","John", "M", "Doe"), null));
    }
}
