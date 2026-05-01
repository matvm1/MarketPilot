package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.util.Tuple;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class JpaPendingVerificationUserRepository implements PendingVerificationUserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Tuple<User, Map<String, Object>>> findByUsername(UserType userType, String username) {
        if (userType == null || username == null || username.isBlank())
            return Optional.empty();

        String registrationPropertyColumns = userType == UserType.CLIENT ? "CLIENT_REGISTRATION_CODE crc, CLIENT_REGISTRATION_EXPIRATION cre" :
                "EMPLOYEE_REGISTRATION_CODE erc, EMPLOYEE_REGISTRATION_EXPIRATION ere";
        // TODO: List all columns
        String sql = "SELECT {u.*}, " + registrationPropertyColumns +
                " FROM APP_USER u JOIN APP_USER_AUTH au ON u.ID = au.USER_ID WHERE u.USERNAME = :username AND " +
                (userType == UserType.CLIENT ? "au.CLIENT_USER_STATUS_ID" : "au.EMPLOYEE_USER_STATUS_ID") +
                " = " +
                UserStatus.PENDING.getCode();

        Session session = entityManager.unwrap(Session.class);
        NativeQuery<Object[]> query = session.createNativeQuery(sql, Object[].class)
                .addEntity(User.class);

        if (userType == UserType.CLIENT) {
            query.addScalar("crc", StandardBasicTypes.STRING);
            query.addScalar("cre", StandardBasicTypes.TIMESTAMP);
        }
        else {
            query.addScalar("erc", StandardBasicTypes.STRING);
            query.addScalar("ere", StandardBasicTypes.TIMESTAMP);
        }

        Optional<Object[]> result = query.setParameter("username", username)
                .uniqueResultOptional();

        if (result.isPresent()) {
            Object[] row = result.get();
            Map<String, Object> registrationProperties = new HashMap<>();
            if (userType == UserType.CLIENT) {
                registrationProperties.put("CLIENT_REGISTRATION_CODE", row[1]);
                registrationProperties.put("CLIENT_REGISTRATION_EXPIRATION", row[2]);
            }
            else {
                registrationProperties.put("EMPLOYEE_REGISTRATION_CODE", row[1]);
                registrationProperties.put("EMPLOYEE_REGISTRATION_EXPIRATION", row[2]);
            }
            return Optional.of(new Tuple<>((User)row[0], registrationProperties));
        }

        return Optional.empty();
    }

    @Override
    public boolean registerNewUser(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) {
        if (userType == null || user == null || roles == null || passwordHash == null || verificationCode == null)
            return false;

        // TODO: Assign roles in service layer
        for (Role role : roles)
            user.grantRole(role);

        entityManager.persist(user);

        int EXPIRATION_PERIOD_MINUTES = 30;
        Timestamp expiration = Timestamp.from(Instant.now().plusSeconds(60 * EXPIRATION_PERIOD_MINUTES));

        entityManager.flush();
        String sql = "INSERT INTO APP_USER_AUTH (" + String.join(", ", generateAuthColumns(userType)) +
                ") VALUES (:userId, :uuid, :isClientOrEmployee, :statusCode, :verificationCode, :expiration, :passwordHash)";

        int rowsAffected = entityManager.createNativeQuery(sql)
                .setParameter("userId", user.getId())
                .setParameter("uuid", user.getUUID())
                .setParameter("isClientOrEmployee", userType == UserType.CLIENT ? user.isClient() : user.isEmployee())
                .setParameter("statusCode", UserStatus.PENDING.getCode())
                .setParameter("verificationCode", verificationCode)
                .setParameter("expiration", expiration)
                .setParameter("passwordHash", passwordHash)
                .executeUpdate();

        return rowsAffected == 1;
    }

    @Override
    public boolean crossRegister(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) {
        if (userType == null || user == null || roles == null || passwordHash == null || verificationCode == null)
            return false;

        // TODO: Assign roles in service layer
        for (Role role : roles)
            user.grantRole(role);

        entityManager.persist(user);

        int EXPIRATION_PERIOD_MINUTES = 30;
        Timestamp expiration = Timestamp.from(Instant.now().plusSeconds(60 * EXPIRATION_PERIOD_MINUTES));

        entityManager.flush();

        String[] authCols = generateAuthColumns(userType);
        String authSql = "UPDATE APP_USER_AUTH SET " +
                IntStream.range(2, authCols.length).mapToObj(i -> authCols[i] + " = :p" + i)
                        .collect(Collectors.joining(", ")) +
                " WHERE USER_ID = :userId";
        int rowsAffected = entityManager.createNativeQuery(authSql)
                .setParameter("p2", userType == UserType.CLIENT ? user.isClient() : user.isEmployee())
                .setParameter("p3", UserStatus.PENDING.getCode())
                .setParameter("p4", verificationCode)
                .setParameter("p5", expiration)
                .setParameter("p6", passwordHash)
                .setParameter("userId", user.getId())
                .executeUpdate();

        return rowsAffected == roles.size();
    }

    @Override
    public boolean completeRegistration(UserType userType, UUID userUUID) {
        String[] authCols = generateAuthColumns(userType);
        String authUpdate = "UPDATE APP_USER_AUTH SET " +
                IntStream.range(3, 6).mapToObj(i -> authCols[i] + " = :p" + i)
                        .collect(Collectors.joining(", ")) +
                " WHERE UUID = :userUuid AND " + authCols[3] + " = :pendingStatusId";
        int rowsAffected = entityManager.createNativeQuery(authUpdate)
                .setParameter("p3", UserStatus.ACTIVE.getCode())
                .setParameter("p4", null)
                .setParameter("p5", null)
                .setParameter("userUuid", userUUID)
                .setParameter("pendingStatusId", UserStatus.PENDING.getCode())
                .executeUpdate();

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

    private String[] generateAuthColumns(UserType userType) {
        String authColPrefix = (userType == UserType.CLIENT ? "CLIENT_" : "EMPLOYEE_");
        String[] authCols = {null, null, null, "USER_STATUS_ID", "REGISTRATION_CODE", "REGISTRATION_EXPIRATION", "PASSWORD_HASH"};
        authCols = Arrays.stream(authCols)
                .map(col -> authColPrefix + col)
                .toArray(String[]::new);
        authCols[0] = "USER_ID";
        authCols[1] = "UUID";
        authCols[2] = userType == UserType.CLIENT ? "IS_CLIENT" : "IS_EMPLOYEE";
        return authCols;
    }
}
