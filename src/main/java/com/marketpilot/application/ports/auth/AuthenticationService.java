package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;

public interface AuthenticationService {
    // Needs to generate a challengeToken that matches the challengeToken expected by the receiving TwoFactorAuthenticationChallenge
    TwoFactorAuthenticationChallenge authenticate(String username, String password);
    AuthenticationResult verifyTwoFactorCode(String username, String twoFactorCode);
}
