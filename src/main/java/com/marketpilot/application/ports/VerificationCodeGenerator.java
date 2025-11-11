package com.marketpilot.application.ports;

public interface VerificationCodeGenerator {
    String generateAlphanumericCode(int length);
}
