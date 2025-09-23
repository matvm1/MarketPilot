package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;
import java.util.UUID;

public interface PendingVerificationUserRepository extends UserRepository {
    // Returns the verification code for the User entity that is pending client account verification
    Optional<String> getClientRegistrationVerificationCode(UUID userUUID);
    // Returns the verification code for the User entity that is pending employee account verification
    Optional<String> getEmployeeRegistrationVerificationCode(UUID userUUID);
}
