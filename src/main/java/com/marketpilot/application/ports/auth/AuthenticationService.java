package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;
import com.marketpilot.domain.entities.auth.User;

public interface AuthenticationService {
    // Needs to generate a challengeToken that matches the challengeToken expected by the receiving TwoFactorAuthenticationChallenge
    TwoFactorAuthenticationChallenge sendChallenge(User user);
    AuthenticationResult verifyTwoFactorCode(User user, String twoFactorCode);
}
