package com.marketpilot.application.ports;

import com.marketpilot.application.services.UserSession;
import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface SessionManager {
    UserSession createSession(User user);
    Optional<UserSession> getSession(String sessionId);
    void invalidate(String sessionId);
}
