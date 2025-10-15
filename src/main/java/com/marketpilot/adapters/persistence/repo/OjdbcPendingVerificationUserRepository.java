package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.adapters.persistence.jdbc.Param;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.util.Tuple;
import com.marketpilot.util.UuidUtil;

import java.util.Optional;
import java.util.UUID;

import static com.marketpilot.adapters.persistence.jdbc.Param.*;
import static com.marketpilot.util.UuidUtil.bytesToUUID;

public class OjdbcPendingVerificationUserRepository implements PendingVerificationUserRepository {
    private final UserFactory userFactory;
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

    public OjdbcPendingVerificationUserRepository() {
        this.userFactory = new UserFactory();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy("USERNAME", stringP(1, username));
    }

    @Override
    public Optional<User> findById(Long id) {
        return findBy("ID", longP(1, id));
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
            //int userId = entityRecord.t();
            User user = entityRecord.u();
            // No need to hydrate with roles as a session won't be created after registration
            //Optional<Set<Role>> rolesOptional = roleRepository.getRolesForUser(userId);
            //rolesOptional.ifPresent(roles -> userFactory.hydrateWithRoles(user, roles));*/
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

    //TODO: JdbcExecutor insert statement
    @Override
    public boolean register(UserType userType, User user, byte[] passwordHash, String verificationCode) {
        if (userType == null)
            return false;

        int rowsAffected = JdbcExecutor.executeUpdate("""
                INSERT INTO APP_PENDING_USER (
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
                    EMPLOYEE_REGISTRATION_CODE
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                );
                """,
            bytesP(1, UuidUtil.uuidToBytes(user.getUUID())),
            stringP(2, user.getEmployeeId()),
            stringP(3, user.getUsername()),
            stringP(4, user.getPersonalEmail()),
            stringP(5, user.getEmployeeEmail()),
            stringP(6, user.getFirstName()),
            stringP(7, user.getMiddleName()),
            stringP(8, user.getLastName()),
            userType == UserType.CLIENT ? bytesP(9, passwordHash) : nullP(9),
            userType == UserType.EMPLOYEE ? bytesP(10, passwordHash) : nullP(10),
            booleanP(11, user.isClient()),
            booleanP(12, user.isEmployee()),
            userType == UserType.CLIENT ? stringP(13, verificationCode) : nullP(14),
            userType == UserType.EMPLOYEE ? stringP(14, verificationCode) : nullP(14)
        );
        //TODO: Insert user roles
        return rowsAffected == 1;
    }

    @Override
    public Optional<String> getClientRegistrationVerificationCode(UUID userUUID) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getEmployeeRegistrationVerificationCode(UUID userUUID) {
        return Optional.empty();
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
