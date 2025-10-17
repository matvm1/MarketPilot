package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.adapters.persistence.jdbc.Param;
import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.util.Tuple;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;

import java.util.*;

import static com.marketpilot.adapters.persistence.jdbc.Param.bytesP;
import static com.marketpilot.adapters.persistence.jdbc.Param.stringP;
import static com.marketpilot.util.UuidUtil.bytesToUUID;
import static com.marketpilot.util.UuidUtil.uuidToBytes;

//TODO: Unit tests
public class OjdbcUserRepository implements UserRepository {
    private final UserFactory userFactory;
    private final RoleRepository roleRepository;
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

    public OjdbcUserRepository(RoleRepository roleRepository) {
        this.userFactory = new UserFactory();
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> findByUUID(UserType userType, UUID uuid) {
        return findBy(userType, "UUID", bytesP(1, uuidToBytes(uuid)));
    }

    @Override
    public Optional<User> findByUsername(UserType userType, String username) {
        return findBy(userType, "USERNAME", stringP(1, username));
    }

    @Override
    public Optional<User> findByEmployeeId(String employeeId) {
        return findBy(UserType.EMPLOYEE, "EMPLOYEE_ID", stringP(1, employeeId));
    }

    @Override
    public Optional<User> findByPersonalEmail(String personalEmail) {
        return findBy(UserType.CLIENT, "PERSONAL_EMAIL", stringP(1, personalEmail));
    }

    @Override
    public Optional<User> findByEmployeeEmail(String employeeEmail) {
        return findBy(UserType.EMPLOYEE, "EMPLOYEE_EMAIL", stringP(1, employeeEmail));
    }

    private Optional<User> findBy(UserType userType, String filterByColumn, Param filterBy) {
        if (filterByColumn == null || filterBy == null || filterBy.value() == null)
            return Optional.empty();

        String fetchSql = buildEntityFetchSql(userType, filterByColumn);
        Optional<Tuple<Integer, User>> entityRecordOptional = JdbcExecutor.fetchRecord(fetchSql,
                rs ->
                        new Tuple<>(rs.getInt("ID"),
                        userFactory.hydrate(
                            bytesToUUID(rs.getBytes("UUID")),
                            rs.getString("EMPLOYEE_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("PERSONAL_EMAIL"),
                            rs.getString("EMPLOYEE_EMAIL"),
                            rs.getString("FIRST_NAME"),
                            rs.getString("MIDDLE_NAME"),
                            rs.getString("LAST_NAME"),
                            rs.getBoolean("IS_CLIENT"),
                            rs.getBoolean("IS_EMPLOYEE")
                            )
                        ), filterBy);
        if (entityRecordOptional.isPresent())
        {
            Tuple<Integer, User> entityRecord = entityRecordOptional.get();
            int userId = entityRecord.t();
            User user = entityRecord.u();
            Optional<Set<Role>> rolesOptional = roleRepository.getRolesForUser(userId);
            rolesOptional.ifPresent(roles -> userFactory.hydrateWithRoles(user, roles));
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private String buildEntityFetchSql(UserType userType, String filterByColumn) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", ENTITY_COLUMN_NAMES))
                .append(" FROM ")
                .append(ENTITY_TABLE_NAME);

        sql.append(" WHERE ")
                .append(userType == UserType.CLIENT ? "CLIENT_USER_STATUS_ID" : "EMPLOYEE_USER_STATUS_ID")
                .append(" = ")
                .append(UserStatus.ACTIVE.getCode());

        if (filterByColumn != null) {
            sql.append(" AND ")
                .append(filterByColumn)
                .append(" = ?");
        }

        return sql.toString();
    }


    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public boolean deleteByUUID(UUID uuid) {
        return false;
    }

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
