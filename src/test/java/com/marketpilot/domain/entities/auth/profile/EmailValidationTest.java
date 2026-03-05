package com.marketpilot.domain.entities.auth.profile;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class EmailValidationTest {

    protected abstract Object createProfile(String email);

    static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of(" ", "blank email"),
                Arguments.of("userexample.com", "missing @ symbol"),
                Arguments.of("user@@example.com", "multiple @ symbols"),
                Arguments.of("@example.com", "empty local part"),
                Arguments.of("user@", "empty domain part"),
                Arguments.of("user..name@example.com", "consecutive dots in local part"),
                Arguments.of(".user@example.com", "local part starting with dot"),
                Arguments.of("user.@example.com", "local part ending with dot"),
                Arguments.of("user name@example.com", "space in local part"),
                Arguments.of("user@localhost", "domain missing TLD"),
                Arguments.of("user@-example.com", "domain starting with hyphen"),
                Arguments.of("user@example-.com", "domain ending with hyphen"),
                Arguments.of("user@example..com", "consecutive dots in domain"),
                Arguments.of("a".repeat(65) + "@example.com", "local part over 64 chars"),
                Arguments.of("user@" + "a".repeat(250) + ".com", "domain over 253 chars")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidEmails")
    void constructor_throwsForInvalidEmail(String email, String description) {
        assertThrows(IllegalArgumentException.class, () -> createProfile(email));
    }
}