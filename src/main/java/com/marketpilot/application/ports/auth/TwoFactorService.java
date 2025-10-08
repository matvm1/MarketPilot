package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;

public interface TwoFactorService {
    boolean verify(MfaCredential credentials);
}
