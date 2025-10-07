package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.User;

public interface EmployeeRepository extends BaseRepository<Long, User> {
    boolean employeeIdExists(String employeeId);
}
