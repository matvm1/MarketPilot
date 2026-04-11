package com.marketpilot.adapters.persistence.repo.springjdbc;

import com.marketpilot.adapters.persistence.util.SqlExceptionBiFunction;
import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.AuthRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiFunction;

public class SpringJdbcAuthRepository implements AuthRepository {
    private JdbcClient jdbcClient;

    public SpringJdbcAuthRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<byte[]> getClientPasswordHash(UUID uuid) {
        return getAuthProperty("CLIENT_PASSWORD_HASH", uuid, tryExtract(ResultSet::getBytes));
    }

    @Override
    public Optional<byte[]> getEmployeePasswordHash(UUID uuid) {
        return getAuthProperty("EMPLOYEE_PASSWORD_HASH", uuid, tryExtract(ResultSet::getBytes));
    }

    @Override
    public Optional<char[]> getClientTotpSecret(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<char[]> getEmployeeTotpSecret(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<MfaType> getMfaType(UUID uuid) {
        return Optional.empty();
    }

    private <U> BiFunction<ResultSet, String, U> tryExtract(SqlExceptionBiFunction<ResultSet, String, U> biFunction) {
        return (resultSet, s) -> {
            try {
                return biFunction.apply(resultSet, s);
            } catch (SQLException e) {
                // TODO: log
                return null;
            }
        };
    }

    private <U> Optional<U> getAuthProperty(String column, UUID uuid, BiFunction<ResultSet, String, U> biFunction) {
        String sql = "SELECT " + column + " FROM APP_USER WHERE UUID = :uuid";
        return jdbcClient.sql(sql)
                .param("uuid", uuid)
                .query((rs, rowNum) -> biFunction.apply(rs, column))
                .optional();
    }

    @Override
    public Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException {
        return Optional.empty();
    }
}
