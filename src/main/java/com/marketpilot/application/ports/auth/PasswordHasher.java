package com.marketpilot.application.ports.auth;

public interface PasswordHasher {
    // hashes password with salt if salt is not null and returns hash, returns null if password is null or 0 length
    byte[] hash(byte[] password);
    // returns true if the two hashes are equivalent and both are non-null, false otherwise
    boolean matches(byte[] password, byte[] hash);
}
