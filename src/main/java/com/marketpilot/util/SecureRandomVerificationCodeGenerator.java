package com.marketpilot.util;

import com.marketpilot.application.ports.VerificationCodeGenerator;

import java.security.SecureRandom;

public class SecureRandomVerificationCodeGenerator implements VerificationCodeGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateAlphanumericCode(int length) {

        if (length < 1) {
            throw new IllegalArgumentException("length must be at least 1");
        }

        // Exclude ambiguous characters: 0, O, 1, I, l
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = SecureRandomVerificationCodeGenerator.secureRandom.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }
}
