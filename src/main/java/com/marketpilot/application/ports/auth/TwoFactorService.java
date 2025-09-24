package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.dto.TwoFactorAuthenticationChallenge;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.Optional;

public interface TwoFactorService {
    // Needs to generate a challengeToken that matches the challengeToken expected by the receiving TwoFactorAuthenticationChallenge
    Optional<TwoFactorAuthenticationChallenge> sendChallenge(String username, RoleName roleName);
    // Returns a properly constructed AuthenticationResult with the user and role they authenticated for, Optional.empty() otherwise
    AuthenticationService.AuthenticationStatus verify(String username, RoleName roleName, String challenge);
}
