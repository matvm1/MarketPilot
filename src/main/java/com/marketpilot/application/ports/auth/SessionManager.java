package com.marketpilot.application.ports.auth;

import com.marketpilot.application.dto.auth.AuthenticationResult;
import com.marketpilot.application.services.UserSession;

import java.util.Optional;

public interface SessionManager {
    Optional<UserSession> createSession(AuthenticationResult authenticationResult);
    //TODO: Consider safer data type for session ids (UUID?)
    Optional<UserSession> getSession(int sessionId);
    void invalidate(int sessionId);
}
