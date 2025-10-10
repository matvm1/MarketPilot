package com.marketpilot.adapters.auth;


import com.marketpilot.application.ports.auth.PasswordHasher;
import com.password4j.Hash;
import com.password4j.Password;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PasswordHasherImpl implements PasswordHasher {
    @Override
    public byte[] hash(byte[] password) throws IllegalArgumentException {
        String PEPPER = System.getenv("MARKETPILOT_PEPPER");

        Hash hash = Password.hash(password)
                .addRandomSalt(32)
                .addPepper(PEPPER)
                .withArgon2();

        PEPPER = null;

        return hash.getBytes();
    }

    @Override
    public boolean matches(byte[] password, byte[] hash) {
        return Password.check(password, hash)
                .withArgon2();
    }
}