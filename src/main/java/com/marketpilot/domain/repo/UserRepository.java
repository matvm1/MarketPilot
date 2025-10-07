package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<Long, User> {
    Optional<User> findByUUID(UUID uuid);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByPersonalEmail(String personalEmail);
    Optional<User> findByEmployeeEmail(String employeeEmail);
    boolean update(User user);
    boolean deleteByUUID(UUID uuid);
    Optional<char[]> getClientPasswordHash(UUID uuid);
    Optional<char[]> getEmployeePasswordHash(UUID uuid);
    Optional<char[]> getClientPasswordSalt(UUID uuid);
    Optional<char[]> getEmployeePasswordSalt(UUID uuid);
}
