package com.marketpilot.domain.entities.auth.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmployeeProfileTest extends EmailValidationTest {
    private EmployeeProfile testEmployeeProfile;
    @BeforeEach
    void setUp() {
        testEmployeeProfile = new EmployeeProfile("ab123456", "johnmdoe@company.com");
    }

    @Override
    protected Object createProfile(String email) {
        return new EmployeeProfile("ab123456", email);
    }

    @Test
    void constructor_throwsForNullEmployeeId() {
        assertThrows(IllegalArgumentException.class, () ->
                new EmployeeProfile(null, "johnmdoe@outlook.com"));
    }

    @Test
    void constructor_throwsForNullEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                new EmployeeProfile("ab123456", null));
    }

    @Test
    void constructor_throwsForBlankEmployeeId() {
        assertThrows(IllegalArgumentException.class, () ->
                new EmployeeProfile("       ", "johnmdoe@outlook.com"));
    }

    @Test
    void setEmployeeId_throwsIfEmployeeIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                testEmployeeProfile.setEmployeeId(null));
    }

    @Test
    void setEmployeeId_throwsIfEmployeeIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                testEmployeeProfile.setEmployeeId("        "));
    }

    @Test
    void setEmail_throwsIfArgIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                testEmployeeProfile.setEmail(null));
    }

    @Test
    void setEmail_throwsIfArgIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                testEmployeeProfile.setEmail("        "));
    }
}
