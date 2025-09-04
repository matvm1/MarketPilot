package com.marketpilot.application.dto;

public record TwoFactorAuthenticationChallenge(String username, String challengeToken) {
    public TwoFactorAuthenticationChallenge {
        if (username == null)
            throw new IllegalArgumentException("principal cannot be null");
        if (username.isBlank())
            throw new IllegalArgumentException("username cannot be blank");
        if (challengeToken == null)
            throw new IllegalArgumentException("challengeToken cannot be null");
        if (challengeToken.isBlank())
            throw new IllegalArgumentException("challengeToken cannot be blank");
    }
}