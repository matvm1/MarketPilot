package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.repo.EmployeeRepository;

import java.util.Optional;

import static com.marketpilot.adapters.persistence.jdbc.Param.stringP;

public class OjdbcEmployeeRepository implements EmployeeRepository {
    @Override
    public boolean employeeIdExists(String employeeId) {
        Optional<String> queryResult = JdbcExecutor.fetchRecord("""
                SELECT EMPLOYEE_ID
                FROM APP_EMPLOYEE
                WHERE EMPLOYEE_ID = ?
                """,
                rs -> rs.getString("EMPLOYEE_ID"),
                stringP(1, employeeId));

        return queryResult.isPresent();
    }

    @Override
    public Optional<User> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean save(User entity) {
        return false;
    }

    @Override
    public int count() {
        return 0;
    }
}
