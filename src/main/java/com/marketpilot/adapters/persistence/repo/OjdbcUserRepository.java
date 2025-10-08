package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.adapters.persistence.jdbc.Param;
import com.marketpilot.adapters.persistence.jdbc.Tuple;
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
    public Optional<User> findByUUID(UUID uuid) {
        return findBy("UUID", bytesP(1, uuidToBytes(uuid)));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy("USERNAME", stringP(1, username));
    }

    @Override
    public Optional<User> findByEmployeeId(String employeeId) {
        return findBy("EMPLOYEE_ID", stringP(1, employeeId));
    }

    @Override
    public Optional<User> findByPersonalEmail(String personalEmail) {
        return findBy("PERSONAL_EMAIL", stringP(1, personalEmail));
    }

    @Override
    public Optional<User> findByEmployeeEmail(String employeeEmail) {
        return findBy("EMPLOYEE_EMAIL", stringP(1, employeeEmail));
    }

    private Optional<User> findBy(String filterByColumn, Param filterBy) {
        if (filterByColumn == null || filterBy == null || filterBy.value() == null)
            return Optional.empty();

        String fetchSql = buildEntityFetchSql(filterByColumn);
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

    private String buildEntityFetchSql(String filterByColumn) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", ENTITY_COLUMN_NAMES))
                .append(" FROM ")
                .append(ENTITY_TABLE_NAME);

        if (filterByColumn != null) {
            sql.append(" WHERE ")
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
    public Optional<char[]> getClientPasswordHash(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT CLIENT_PASSWORD_HASH
            FROM APP_USER
            WHERE UUID = ?
            """,
            rs -> {
                String result = rs.getString("CLIENT_PASSWORD_HASH");
                if (result == null)
                    return null;
                return result.toCharArray();
            },
            bytesP(1, uuidToBytes(uuid)
        ));
    }

    @Override
    public Optional<char[]> getEmployeePasswordHash(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT EMPLOYEE_PASSWORD_HASH
            FROM APP_USER
            WHERE UUID = ?
            """,
            rs -> {
                String result = rs.getString("EMPLOYEE_PASSWORD_HASH");
                if (result == null)
                    return null;
                return result.toCharArray();
            },
            bytesP(1, uuidToBytes(uuid)
        ));
    }

    @Override
    public Optional<char[]> getClientPasswordSalt(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT CLIENT_PASSWORD_SALT
            FROM APP_USER
            WHERE UUID = ?
            """,
            rs -> {
                String result = rs.getString("CLIENT_PASSWORD_SALT");
                if (result == null)
                    return null;
                return result.toCharArray();
            },
            bytesP(1, uuidToBytes(uuid)
        ));
    }

    @Override
    public Optional<char[]> getEmployeePasswordSalt(UUID uuid) {
        if (uuid == null)
            return Optional.empty();

        return JdbcExecutor.fetchRecord("""
            SELECT EMPLOYEE_PASSWORD_SALT
            FROM APP_USER
            WHERE UUID = ?
            """,
            rs -> {
                String result = rs.getString("EMPLOYEE_PASSWORD_SALT");
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
