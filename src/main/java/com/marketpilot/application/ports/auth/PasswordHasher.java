package com.marketpilot.application.ports.auth;

public interface PasswordHasher {
    String hash(char[] rawPassword);
    boolean matches(String passwordHashProvided, String passwordHashExpected);
}
