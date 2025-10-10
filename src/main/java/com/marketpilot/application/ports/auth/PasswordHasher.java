package com.marketpilot.application.ports.auth;

public interface PasswordHasher {
    // hashes password with salt if salt is not null and returns hash, throws IllegalArgumentException if either are null
    byte[] hash(byte[] password) throws IllegalArgumentException;
    // returns true if the two hashes are equivalent and both are non-null, false otherwise
    boolean matches(byte[] password, byte[] hash);
}
