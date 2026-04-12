package com.marketpilot.adapters.persistence.repo.springjdbc;

import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.repo.EmployeeRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

public class SpringJdbcEmployeeRepository implements EmployeeRepository {
    private final JdbcClient jdbcClient;

    public SpringJdbcEmployeeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean employeeIdExists(String employeeId) {
        Optional<String> queryResult = jdbcClient.sql("SELECT EMPLOYEE_ID FROM APP_EMPLOYEE WHERE EMPLOYEE_ID = :employeeId")
                .param("employeeId", employeeId)
                .query((rs, rowNum) -> rs.getString("EMPLOYEE_ID"))
                .optional();

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
