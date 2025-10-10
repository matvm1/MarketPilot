package com.marketpilot.application.ports.auth;

public interface PasswordHasher {
    // hashes password with salt if salt is not null and returns hash, throws IllegalArgumentException if either are null
    char[] hash(char[] password, char[] salt) throws IllegalArgumentException;
    // returns true if the two hashes are equivalent and both are non-null, false otherwise
    boolean matches(char[] password, char[] salt, char[] hash);
}
