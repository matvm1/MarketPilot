package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.util.Tuple;

import java.sql.SQLException;
import java.util.*;

public interface PendingVerificationUserRepository extends BaseRepository<Long, User> {
    Optional<Tuple<User, Map<String, Object>>> findByUsername(UserType userType, String username);
    boolean register(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException;
    boolean completeRegistration(UserType userType, UUID userUUID) throws SQLException;
    // Returns the verification code for the User entity that is pending client account verification
    Optional<String> getClientRegistrationVerificationCode(UUID userUUID);
    // Returns the verification code for the User entity that is pending employee account verification
    Optional<String> getEmployeeRegistrationVerificationCode(UUID userUUID);
    boolean deleteByUuid(UUID uuid);
}
