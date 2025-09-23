package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByPersonalEmail(String personalEmail);
    Optional<User> findByEmployeeEmail(String employeeEmail);
    boolean deleteByUUID(UUID uuid);
}
