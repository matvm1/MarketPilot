package com.marketpilot.adapters.persistence.repo.springjdbc;

import com.marketpilot.adapters.persistence.util.SqlExceptionBiFunction;
import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.AuthRepository;
import com.marketpilot.util.BufferedConverter;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        return getAuthProperty("CLIENT_TOTP_SECRET", uuid, tryExtract(ResultSet::getBytes), BufferedConverter::toChars);
    }

    @Override
    public Optional<char[]> getEmployeeTotpSecret(UUID uuid) {
        return getAuthProperty("EMPLOYEE_TOTP_SECRET", uuid, tryExtract(ResultSet::getBytes), BufferedConverter::toChars);
    }

    @Override
    public Optional<MfaType> getMfaType(UUID uuid) {
        String sql = "SELECT mfa.NAME FROM MFA_TYPE mfa JOIN APP_USER u ON mfa.ID = u.MFA_TYPE_ID WHERE UUID = :uuid";
        return getAuthProperty(sql, "MFA_TYPE", uuid, tryExtract(ResultSet::getString), MfaType::valueOf);

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

    private <U> Optional<U> getAuthProperty(String sql, String column, UUID uuid, BiFunction<ResultSet, String, U> mapper) {
        return jdbcClient.sql(sql)
                .param("uuid", uuid)
                .query((rs, rowNum) -> mapper.apply(rs, column))
                .optional();
    }

    private <T, U> Optional<U> getAuthProperty(String sql, String column, UUID uuid, BiFunction<ResultSet, String, T> mapper, Function<T, U> postProcessor) {
        return getAuthProperty(sql, column, uuid, mapper)
                .map(postProcessor);
    }

    private <U> Optional<U> getAuthProperty(String column, UUID uuid, BiFunction<ResultSet, String, U> mapper) {
        String sql = "SELECT " + column + " FROM APP_USER WHERE UUID = :uuid";
        return getAuthProperty(sql, column, uuid, mapper);
    }

    private <T, U> Optional<U> getAuthProperty(String column, UUID uuid, BiFunction<ResultSet, String, T> mapper, Function<T, U> postProcessor) {
        String sql = "SELECT " + column + " FROM APP_USER WHERE UUID = :uuid";
        return getAuthProperty(sql, column, uuid, mapper, postProcessor);
    }

    @Override
    public Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException {
        return Optional.empty();
    }
}
