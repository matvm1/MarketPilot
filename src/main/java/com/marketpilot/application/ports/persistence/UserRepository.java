package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByPersonalEmail(String personalEmail);
    Optional<User> findByEmployeeEmail(String employeeEmail);
    //TODO: delete based on a better performing attribute i.e. ID/UUID
    boolean deleteByUsername(String username);
}
