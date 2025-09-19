package com.marketpilot.application.ports;

import com.marketpilot.application.dto.AuthenticationResult;
import com.marketpilot.application.services.UserSession;
import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface SessionManager {
    UserSession createSession(AuthenticationResult authenticationResult);
    //TODO: Consider safer data type for session ids (UUID?)
    Optional<UserSession> getSession(int sessionId);
    void invalidate(int sessionId);
}
