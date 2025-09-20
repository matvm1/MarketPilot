package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;
import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface TwoFactorService {
    // Needs to generate a challengeToken that matches the challengeToken expected by the receiving TwoFactorAuthenticationChallenge
    TwoFactorAuthenticationChallenge sendChallenge(User user);
    Optional<AuthenticationResult> verify(User user, String challenge);
}
