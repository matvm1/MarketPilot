package com.marketpilot.adapters.persistence.repo.ojdbc;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.AuthRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static com.marketpilot.adapters.persistence.jdbc.Param.bytesP;
import static com.marketpilot.adapters.persistence.jdbc.Param.intP;
import static com.marketpilot.domain.entities.auth.UserType.CLIENT;
import static com.marketpilot.util.UuidUtil.uuidToBytes;

@Deprecated
public class OjdbcAuthRepository implements AuthRepository {
    @Override
    public Optional<byte[]> getClientPasswordHash(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT CLIENT_PASSWORD_HASH
            FROM APP_USER
            WHERE UUID = ?
            """,
                rs -> rs.getBytes("CLIENT_PASSWORD_HASH"),
                bytesP(1, uuidToBytes(uuid)
                ));
    }

    @Override
    public Optional<byte[]> getEmployeePasswordHash(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT EMPLOYEE_PASSWORD_HASH
            FROM APP_USER
            WHERE UUID = ?
            """,
                rs -> rs.getBytes("EMPLOYEE_PASSWORD_HASH"),
                bytesP(1, uuidToBytes(uuid)
                ));
    }

    @Override
    public Optional<char[]> getClientTotpSecret(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT CLIENT_TOTP_SECRET
            FROM APP_USER
            WHERE UUID = ?
            """,
                rs -> {
                    String result = rs.getString("CLIENT_TOTP_SECRET");
                    if (result == null)
                        return null;
                    return result.toCharArray();
                },
                bytesP(1, uuidToBytes(uuid)
                ));
    }

    @Override
    public Optional<char[]> getEmployeeTotpSecret(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT EMPLOYEE_TOTP_SECRET
            FROM APP_USER
            WHERE UUID = ?
            """,
                rs -> {
                    String result = rs.getString("EMPLOYEE_TOTP_SECRET");
                    if (result == null)
                        return null;
                    return result.toCharArray();
                },
                bytesP(1, uuidToBytes(uuid)
                ));
    }

    @Override
    public Optional<MfaType> getMfaType(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        Optional<Integer> mfaTypeIdOptional = JdbcExecutor.fetchRecord("""
                SELECT MFATYPE_ID
                FROM APP_USER
                WHERE UUID = ?
                """,
                rs -> rs.getInt(1),
                bytesP(1, uuidToBytes(uuid)));

        return mfaTypeIdOptional.map(MfaType::fromCode);
    }

    public Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException {
        if (userType == null || uuid == null)
            return Optional.empty();

        String passwordHashCol = userType == CLIENT ? "CLIENT_PASSWORD_HASH" : "EMPLOYEE_PASSWORD_HASH";
        String totpSecretCol = userType == CLIENT ? "CLIENT_TOTP_SECRET" : "EMPLOYEE_TOTP_SECRET";
        String columns = passwordHashCol + ", " + totpSecretCol + ", MFATYPE_ID";
        Properties p = JdbcExecutor.fetchToObject(String.format("""
                SELECT %s
                FROM APP_USER
                WHERE %S = ?
                AND UUID = ?
                """, columns, userType == CLIENT ? "CLIENT_USER_STATUS_ID" : "EMPLOYEE_USER_STATUS_ID"),
                (rs) -> {
                    rs.next();
                    if (rs.isFirst()) {
                        Properties res = new Properties(3);
                        String passwordHash = rs.getString(passwordHashCol);
                        if (passwordHash != null)
                            res.put("PASSWORD_HASH", passwordHash);
                        String totpSecret = rs.getString(totpSecretCol);
                        if (totpSecret != null)
                            res.put("TOTP_SECRET", totpSecret);
                        int mfaTypeId = rs.getInt("MFATYPE_ID");
                        if (mfaTypeId != 0)
                            res.put("MFATYPE", MfaType.fromCode(mfaTypeId));
                        return res;
                    }
                    else
                        return null;
                }, intP(1, UserStatus.ACTIVE.getCode()), bytesP(2, uuidToBytes(uuid))
        ).orElse(null);

        return Optional.ofNullable(p);
    }
}
