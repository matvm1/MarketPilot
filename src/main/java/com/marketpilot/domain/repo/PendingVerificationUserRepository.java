package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.Optional;
import java.util.UUID;

public interface PendingVerificationUserRepository extends BaseRepository<Long, User> {
    Optional<User> findByUsername(String username);
    boolean register(UserType userType, User user, byte[] passwordHash, String verificationCode);
    // Returns the verification code for the User entity that is pending client account verification
    Optional<String> getClientRegistrationVerificationCode(UUID userUUID);
    // Returns the verification code for the User entity that is pending employee account verification
    Optional<String> getEmployeeRegistrationVerificationCode(UUID userUUID);
    boolean deleteByUuid(UUID uuid);
}
