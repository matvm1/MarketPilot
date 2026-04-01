package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public interface UserRepository extends BaseRepository<Long, User> {
    Optional<User> findByUUID(UserType userType, UUID uuid);
    Optional<User> findByUsername(UserType userType, String username);
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByPersonalEmail(String personalEmail);
    Optional<User> findByEmployeeEmail(String employeeEmail);
    boolean deleteByUUID(UUID uuid);
}
