package com.marketpilot.domain.entities.auth.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class EmailValidationTest {

    protected abstract Object createProfile(String email);

    @Test
    void constructor_throwsForInvalidEmailPatterns() {
        // Blank email (whitespace only)
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile(" "),
                "Expected constructor to throw for blank email");
        // Missing @ symbol
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("userexample.com"),
                "Expected constructor to throw for email missing @ symbol");
        // Multiple @ symbols
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@@example.com"),
                "Expected constructor to throw for email with multiple @ symbols");
        // Empty local part (before @)
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("@example.com"),
                "Expected constructor to throw for email with empty local part");
        // Empty domain part (after @)
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@"),
                "Expected constructor to throw for email with empty domain part");
        // Consecutive dots in local part
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user..name@example.com"),
                "Expected constructor to throw for email with consecutive dots in local part");
        // Local part starting with dot
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile(".user@example.com"),
                "Expected constructor to throw for email with local part starting with dot");
        // Local part ending with dot
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user.@example.com"),
                "Expected constructor to throw for email with local part ending with dot");
        // Invalid character (space) in local part
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user name@example.com"),
                "Expected constructor to throw for email with space in local part");
        // Domain without TLD
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@localhost"),
                "Expected constructor to throw for email with domain missing TLD");
        // Domain starting with hyphen
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@-example.com"),
                "Expected constructor to throw for email with domain starting with hyphen");
        // Domain ending with hyphen
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@example-.com"),
                "Expected constructor to throw for email with domain ending with hyphen");
        // Consecutive dots in domain
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@example..com"),
                "Expected constructor to throw for email with consecutive dots in domain");
        // Local part too long (over 64 characters)
        String longLocalPart = "a".repeat(65);
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile(longLocalPart + "@example.com"),
                "Expected constructor to throw for email with local part exceeding 64 characters");
        // Domain too long (over 253 characters total)
        String longDomain = "a".repeat(250) + ".com";
        assertThrows(IllegalArgumentException.class, () ->
                        createProfile("user@" + longDomain),
                "Expected constructor to throw for email with domain exceeding 253 characters");
    }
}
