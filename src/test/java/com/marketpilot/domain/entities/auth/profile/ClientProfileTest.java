package com.marketpilot.domain.entities.auth.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClientProfileTest extends EmailValidationTest {
    private ClientProfile testClientProfile;
    @BeforeEach
    void setUp() {
        testClientProfile = new ClientProfile("johnmdoe@outlook.com");
    }

    @Override
    protected Object createProfile(String email) {
        return new ClientProfile(email);
    }

    @Test
    void constructor_throwsForNullEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                        new ClientProfile(null));
    }

    @Test
    void setEmail_throwsIfArgIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                testClientProfile.setEmail(null));
    }

    @Test
    void setEmail_throwsIfArgIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                testClientProfile.setEmail("        "));
    }
}
