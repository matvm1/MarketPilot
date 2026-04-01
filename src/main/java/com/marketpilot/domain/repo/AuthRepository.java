package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.UserType;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public interface AuthRepository {
    Optional<byte[]> getClientPasswordHash(UUID uuid);

    Optional<byte[]> getEmployeePasswordHash(UUID uuid);

    Optional<char[]> getClientTotpSecret(UUID uuid);

    Optional<char[]> getEmployeeTotpSecret(UUID uuid);

    Optional<MfaType> getMfaType(UUID uuid);

    Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException;
}
