package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface EmployeeRepository extends BaseRepository<User, Long> {
    boolean employeeIdExists(String employeeId);
}
