package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface PendingVerificationUserRepository extends UserRepository {
    //TODO: Find by a faster performing search criteria i.e. ID/UUID
    // Returns the verification code for the User entity that is pending account verification
    Optional<String> getVerificationCode(String username);
}
