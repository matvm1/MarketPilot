package com.marketpilot.application.ports.auth;

public interface TotpService extends TwoFactorService {
    public String generateSecret();
}
