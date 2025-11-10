package com.marketpilot.adapters.auth;


import com.marketpilot.application.ports.auth.PasswordHasher;
import com.password4j.BadParametersException;
import com.password4j.Hash;
import com.password4j.Password;

public class Password4JHasher implements PasswordHasher {
    @Override
    public byte[] hash(byte[] password) {
        try {
            String PEPPER = System.getenv("MARKETPILOT_PEPPER");

            Hash hash = Password.hash(password)
                    .addRandomSalt(32)
                    .addPepper(PEPPER)
                    .withArgon2();

            PEPPER = null;

            return hash.getResultAsBytes();
        }
        catch (BadParametersException e) {
            return null;
        }
    }

    @Override
    public boolean matches(byte[] password, byte[] hash) {
        try {
            String PEPPER = System.getenv("MARKETPILOT_PEPPER");

            return Password.check(password, hash)
                    .addPepper(PEPPER)
                    .withArgon2();
        }
        catch (BadParametersException e) {
            return false;
        }
    }
}