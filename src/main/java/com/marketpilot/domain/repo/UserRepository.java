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
    boolean update(User user);
    boolean deleteByUUID(UUID uuid);
    Optional<byte[]> getClientPasswordHash(UUID uuid);
    Optional<byte[]> getEmployeePasswordHash(UUID uuid);
    Optional<char[]> getClientTotpSecret(UUID uuid);
    Optional<char[]> getEmployeeTotpSecret(UUID uuid);
    Optional<MfaType> getMfaType(UUID uuid);
    public Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException;
}
