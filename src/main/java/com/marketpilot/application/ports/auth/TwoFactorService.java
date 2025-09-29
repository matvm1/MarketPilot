package com.marketpilot.application.ports.auth;

import com.marketpilot.application.services.AuthenticationService.AuthenticationStatus;
import com.marketpilot.domain.entities.auth.Role.RoleName;

public interface TwoFactorService {
    // Returns a properly constructed AuthenticationResult with the user and role they authenticated for, Optional.empty() otherwise
    AuthenticationStatus verify(String username, RoleName roleName, String challenge);
}
