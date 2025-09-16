package com.marketpilot.application.ports;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPersonalEmail(String personalEmail);
    Optional<User> findByEmployeeEmail(String employeeEmail);
}
