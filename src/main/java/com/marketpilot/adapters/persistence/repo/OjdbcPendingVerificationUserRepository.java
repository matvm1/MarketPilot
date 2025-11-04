package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.adapters.persistence.jdbc.Param;
import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.util.Tuple;
import oracle.jdbc.internal.OracleTimestamp;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.marketpilot.adapters.persistence.jdbc.Param.*;
import static com.marketpilot.util.UuidUtil.bytesToUUID;
import static com.marketpilot.util.UuidUtil.uuidToBytes;

public class OjdbcPendingVerificationUserRepository implements PendingVerificationUserRepository {
    private final UserFactory userFactory;
    private final RoleCache roleCache;
    private final String ENTITY_TABLE_NAME = "APP_USER";
    private final String[] ENTITY_COLUMN_NAMES = {
            "ID",
            "UUID",
            "EMPLOYEE_ID",
            "USERNAME",
            "PERSONAL_EMAIL",
            "EMPLOYEE_EMAIL",
            "FIRST_NAME",
            "MIDDLE_NAME",
            "LAST_NAME",
            "IS_CLIENT",
            "IS_EMPLOYEE"
    };
    private final String[] CLIENT_REGISTRATION_PROPERTIES = {
            "CLIENT_REGISTRATION_CODE",
            "CLIENT_REGISTRATION_EXPIRATION"
    };
    private final String[] EMPLOYEE_REGISTRATION_PROPERTIES = {
            "EMPLOYEE_REGISTRATION_CODE",
            "EMPLOYEE_REGISTRATION_EXPIRATION"
    };

    public OjdbcPendingVerificationUserRepository(RoleCache roleCache) {
        this.userFactory = new UserFactory();
        this.roleCache = roleCache;
    }

    @Override
    public Optional<Tuple<User, Map<String, Object>>> findByUsername(UserType userType, String username) {
        return findBy(userType, "USERNAME", stringP(1, username));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    private Optional<Tuple<User, Map<String, Object>>> findBy(UserType userType, String filterByColumn, Param filterBy) {
        if (filterByColumn == null || filterBy == null || filterBy.value() == null)
            return Optional.empty();

        String[] registrationPropertyColumns = userType == UserType.CLIENT ? CLIENT_REGISTRATION_PROPERTIES : EMPLOYEE_REGISTRATION_PROPERTIES;
        String fetchSql = buildEntityFetchSql(userType, filterByColumn, registrationPropertyColumns);
        Optional<Tuple<User, Map<String, Object>>> entityRecordOptional = null;
        try {
            entityRecordOptional = JdbcExecutor.fetchToObject(fetchSql,
                    rs -> {
                        rs.next();
                        if (rs.isFirst()) {
                            User user = userFactory.hydrate(
                                    bytesToUUID(rs.getBytes("UUID")),
                                    rs.getString("EMPLOYEE_ID"),
                                    rs.getString("USERNAME"),
                                    rs.getString("PERSONAL_EMAIL"),
                                    rs.getString("EMPLOYEE_EMAIL"),
                                    rs.getString("FIRST_NAME"),
                                    rs.getString("MIDDLE_NAME"),
                                    rs.getString("LAST_NAME"),
                                    rs.getBoolean("IS_CLIENT"),
                                    rs.getBoolean("IS_EMPLOYEE"));
                            Map<String, Object> registrationProperties = new HashMap<>();
                            for (String column : registrationPropertyColumns) {
                                if (column.endsWith("CODE"))
                                    registrationProperties.put(column, rs.getString(column));
                                else
                                    registrationProperties.put(column, rs.getTimestamp(column).toInstant());
                            }
                            return new Tuple<>(user, registrationProperties);
                        }
                        else
                            return null;
                    },
            filterBy);
        } catch (SQLException e) {
            return Optional.empty();
        }
        // No need to hydrate with roles as a session won't be created after registration
        return entityRecordOptional;
    }

    private String buildEntityFetchSql(UserType userType, String filterByColumn, String[] registrationPropertyColumns) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", ENTITY_COLUMN_NAMES));
        if (registrationPropertyColumns != null)
            sql.append(", ").append(String.join(", ", registrationPropertyColumns));

        sql.append(" FROM ").append(ENTITY_TABLE_NAME);

        sql.append(" WHERE ")
                .append(userType == UserType.CLIENT ? "CLIENT_USER_STATUS_ID" : "EMPLOYEE_USER_STATUS_ID")
                .append(" = ")
                .append(UserStatus.PENDING.getCode());

        if (filterByColumn != null) {
            sql.append(" AND ")
                    .append(filterByColumn)
                    .append(" = ?");
        }

        return sql.toString();
    }

    // registers a new user
    @Override
    public boolean registerNewUser(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException {
        if (userType == null)
            return false;

        int EXPIRATION_PERIOD_MINUTES = 30;
        Timestamp expiration = Timestamp.from(Instant.now().plusSeconds(60 * EXPIRATION_PERIOD_MINUTES));
        long[] generatedKeys = JdbcExecutor.executeInsert("""
                INSERT INTO APP_USER (
                    UUID,
                    EMPLOYEE_ID,
                    USERNAME,
                    PERSONAL_EMAIL,
                    EMPLOYEE_EMAIL,
                    FIRST_NAME,
                    MIDDLE_NAME,
                    LAST_NAME,
                    CLIENT_PASSWORD_HASH,
                    EMPLOYEE_PASSWORD_HASH,
                    IS_CLIENT,
                    IS_EMPLOYEE,
                    CLIENT_REGISTRATION_CODE,
                    EMPLOYEE_REGISTRATION_CODE,
                    CLIENT_USER_STATUS_ID,
                    EMPLOYEE_USER_STATUS_ID,
                    CLIENT_REGISTRATION_EXPIRATION,
                    EMPLOYEE_REGISTRATION_EXPIRATION
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """,
            bytesP(1, uuidToBytes(user.getUUID())),
            stringP(2, user.getEmployeeId()),
            stringP(3, user.getUsername()),
            stringP(4, user.getPersonalEmail()),
            stringP(5, user.getEmployeeEmail()),
            stringP(6, user.getFirstName()),
            stringP(7, user.getMiddleName()),
            stringP(8, user.getLastName()),
            paramElseNull(userType, UserType.CLIENT, bytesP(9, passwordHash)),
            paramElseNull(userType, UserType.EMPLOYEE, bytesP(10, passwordHash)),
            booleanP(11, user.isClient()),
            booleanP(12, user.isEmployee()),
            paramElseNull(userType, UserType.CLIENT, stringP(13, verificationCode)),
            paramElseNull(userType, UserType.EMPLOYEE, stringP(14, verificationCode)),
            paramElseNull(userType, UserType.CLIENT, intP(15, UserStatus.PENDING.getCode())),
            paramElseNull(userType, UserType.EMPLOYEE, intP(16, UserStatus.PENDING.getCode())),
            paramElseNull(userType, UserType.CLIENT, timestampP(17, expiration)),
            paramElseNull(userType, UserType.EMPLOYEE, timestampP(18, expiration))
        );

        int rolesInserted = 0;
        if (generatedKeys.length == 1) {
            long userId = generatedKeys[0];
            rolesInserted = insertRoles(userId, expiration, roles);
        }
        return generatedKeys.length == 1 && rolesInserted == roles.size();
    }

    @Override
    public boolean crossRegister(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException {
        if (userType == null)
            return false;

        int EXPIRATION_PERIOD_MINUTES = 30;
        Timestamp expiration = Timestamp.from(Instant.now().plusSeconds(60 * EXPIRATION_PERIOD_MINUTES));
        long[] generatedKeys = userType == UserType.CLIENT ? registerEmployeeAsClient(user, passwordHash, verificationCode, expiration)
                : registerClientAsEmployee(user, passwordHash, verificationCode, expiration);

        int rolesInserted = 0;
        if (generatedKeys.length == 1) {
            long userId = generatedKeys[0];
            rolesInserted = insertRoles(userId, expiration, roles);
        }
        return generatedKeys.length == 1 && rolesInserted == roles.size();
    }

    private long[] registerClientAsEmployee(User user, byte[] passwordHash, String verificationCode, Timestamp expiration) throws SQLException {
        return JdbcExecutor.executeInsert("""
                UPDATE APP_USER
                SET EMPLOYEE_ID = ?,
                    EMPLOYEE_EMAIL = ?,
                    EMPLOYEE_PASSWORD_HASH = ?,
                    IS_EMPLOYEE = ?,
                    EMPLOYEE_REGISTRATION_CODE = ?,
                    EMPLOYEE_USER_STATUS_ID = ?,
                    EMPLOYEE_REGISTRATION_EXPIRATION = ?
                WHERE UUID = ?
                """,
                stringP(1, user.getEmployeeId()),
                stringP(2, user.getEmployeeEmail()),
                bytesP(3, passwordHash),
                booleanP(4, user.isEmployee()),
                stringP(5, verificationCode),
                intP(6, UserStatus.PENDING.getCode()),
                timestampP(7, expiration),
                bytesP(8, uuidToBytes(user.getUUID()))
        );
    }

    private long[] registerEmployeeAsClient(User user, byte[] passwordHash, String verificationCode, Timestamp expiration) throws SQLException {
        return JdbcExecutor.executeInsert("""
                UPDATE APP_USER
                SET PERSONAL_EMAIL = ?,
                   CLIENT_PASSWORD_HASH = ?,
                   IS_CLIENT = ?,
                   CLIENT_REGISTRATION_CODE = ?,
                   CLIENT_USER_STATUS_ID = ?,
                   CLIENT_REGISTRATION_EXPIRATION = ?
                WHERE UUID = ?
                """,
                stringP(1, user.getPersonalEmail()),
                bytesP(2, passwordHash),
                booleanP(3, user.isClient()),
                stringP(4, verificationCode),
                intP(5, UserStatus.PENDING.getCode()),
                timestampP(6, expiration),
                bytesP(7, uuidToBytes(user.getUUID()))
        );
    }

        private int insertRoles(long userId, Timestamp expiration, Set<Role> roles) throws SQLException {
        StringBuilder binders = new StringBuilder();
        binders.append("(?, ?, ?),".repeat(Math.max(0, roles.size() - 1)));
        binders.append("(?, ?, ?);");

        Param[] params = new Param[roles.size() * 3];
        int i = 0;
        for (Role role : roles) {
            params[i] = longP(i + 1, userId);
            params[i + 1] = intP(i + 2, roleCache.getId(role.getRoleName()));
            params[i + 2] = timestampP(i + 3, expiration);
            i += 3;
        }

        return JdbcExecutor.executeUpdate("""
                    INSERT INTO APP_USER_ROLE (
                        USER_ID,
                        ROLE_ID,
                        REGISTRATION_EXPIRATION
                    )
                    VALUES
                    """ + binders,
                params);
    }

    @Override
    public boolean completeRegistration(UserType userType, UUID userUUID) throws SQLException {
        if (userType == null || userUUID == null)
            return false;

        String statusColumn = userType == UserType.CLIENT ? "CLIENT_USER_STATUS_ID" : "EMPLOYEE_USER_STATUS_ID";
        String expirationColumn = userType == UserType.CLIENT ? "CLIENT_REGISTRATION_EXPIRATION" : "EMPLOYEE_REGISTRATION_EXPIRATION";
        String verificationCodeColumn = userType == UserType.CLIENT ? "CLIENT_REGISTRATION_CODE" : "EMPLOYEE_REGISTRATION_CODE";
        int result = JdbcExecutor.executeUpdate(String.format("""
                UPDATE APP_USER
                SET %s = ?,
                    %s = ?,
                    %s = ?
                WHERE %s = ?
                AND UUID = ?
                AND SYSTIMESTAMP <= %s
                """, statusColumn, expirationColumn, verificationCodeColumn, statusColumn, expirationColumn),
                intP(1, UserStatus.ACTIVE.getCode()),
                nullP(2),
                nullP(3),
                intP(4, UserStatus.PENDING.getCode()),
                bytesP(5, uuidToBytes(userUUID)));

        return result == 1;
    }

    private Param paramElseNull(UserType supplied, UserType expected, Param forUserType) {
        return supplied == expected ? forUserType : nullP(forUserType.index());
    }

    @Override
    public Optional<String> getClientRegistrationVerificationCode(UUID userUUID) {
        return JdbcExecutor.fetchRecord("""
                SELECT CLIENT_REGISTRATION_CODE
                FROM APP_USER
                WHERE CLIENT_USER_STATUS_ID = ?
                AND UUID = ?
                """,
                rs -> rs.getString("CLIENT_REGISTRATION_CODE"),
                intP(1, UserStatus.PENDING.getCode()),
                bytesP(2, uuidToBytes(userUUID)));
    }

    @Override
    public Optional<String> getEmployeeRegistrationVerificationCode(UUID userUUID) {
        return JdbcExecutor.fetchRecord("""
                SELECT EMPLOYEE_REGISTRATION_CODE
                FROM APP_USER
                WHERE EMPLOYEE_USER_STATUS_ID = ?
                AND UUID = ?
                """,
                rs -> rs.getString("EMPLOYEE_REGISTRATION_CODE"),
                intP(1, UserStatus.PENDING.getCode()),
                bytesP(2, uuidToBytes(userUUID)));
    }

    @Override
    public boolean deleteByUuid(UUID uuid) {
        return false;
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
