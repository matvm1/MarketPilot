package com.marketpilot.domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Name Validation Tests")
class UserTest {
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "johnmdoe", "John", "M", "Doe");
    }

    @Test
    void constructor_throwsForNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(0, "johnmdoe", "John", "M", "Doe"));
    }

    @Test
    void constructor_throwsForNullUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, null, "John", "M", "Doe"),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForBlankUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, "     ", "John", "M", "Doe"),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForNullFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, "johnmdoe", null, "M", "Doe"),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, "johnmdoe", "      ", "M", "Doe"));
    }

    @Test
    void constructor_throwsForNullLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, "johnmdoe", "John", "M", null));
    }

    @Test
    void constructor_throwsForBlankLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, "johnmdoe", "John", "M", ""));
    }

    @Test
    void grantRoles_throwsForNullUserRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                testUser.grantRoles(null));
    }

    @Test
    void grantRoles_throwsForEmptyUserRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                testUser.grantRoles(new HashSet<>()));
    }

    @Test
    void setFirstName_throwsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName(null));
    }

    @Test
    void setFirstName_throwsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName("     "));
    }

    @Test
    void setLastName_throwsForNullString() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName(null));
    }

    @Test
    void setLastName_throwsForBlankString() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName("\n"));
    }
}
