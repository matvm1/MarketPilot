package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;

public interface TwoFactorService {
    // Returns a properly constructed AuthenticationResult with the user and role they authenticated for, Optional.empty() otherwise
    boolean verify(MfaCredential credentials);
}
