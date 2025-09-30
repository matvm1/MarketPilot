package com.marketpilot.application.dto.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Deprecated
class TwoFactorChallengeTest {
    @Test
    void constructor_throwsForNullUserName() {
        assertThrows(IllegalArgumentException.class, () ->
                new TwoFactorAuthenticationChallenge(null, "a1b2c3d4-e5f6-7890-1234-567890abcdef"));
    }

    @Test
    void constructor_throwsForBlankUserName() {
        assertThrows(IllegalArgumentException.class, () ->
                new TwoFactorAuthenticationChallenge("   ", "a1b2c3d4-e5f6-7890-1234-567890abcdef"));
    }

    @Test
    void constructor_throwsForNullChallengeToken() {
        assertThrows(IllegalArgumentException.class, () ->
                new TwoFactorAuthenticationChallenge("johnmdoe", null));
    }

    @Test
    void constructor_throwsForBlankChallengeToken() {
        assertThrows(IllegalArgumentException.class, () ->
                new TwoFactorAuthenticationChallenge("johnmdoe", "          "));
    }
}
