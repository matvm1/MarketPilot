package com.marketpilot.application.ports.auth;

public interface PasswordHasher {
    boolean matches(String passwordHashProvided, String passwordHashExpected);
}
