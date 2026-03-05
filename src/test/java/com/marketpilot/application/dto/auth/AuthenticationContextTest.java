package com.marketpilot.application.dto.auth;

import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationContextTest {
    @Test
    void constructor_throwsForNullPrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
                        new AuthenticationContext(null, TestRoles.PERSONAL_INVESTOR_ROLE));
    }

    @Test
    void constructor_throwsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new AuthenticationContext(Mockito.mock(User.class), null));
    }
}
