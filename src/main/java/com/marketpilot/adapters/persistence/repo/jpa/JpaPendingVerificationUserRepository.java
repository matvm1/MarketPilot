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

import java.sql.SQLException;
import java.util.*;

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

    // WIP
    @Override
    public boolean registerNewUser(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException {
        if (userType == null || user == null || roles == null || passwordHash == null || verificationCode == null)
            return false;

        for (Role role : roles)
            user.grantRole(role);

        entityManager.persist(user);

        return false;
    }

    @Override
    public boolean crossRegister(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException {
        return false;
    }

    @Override
    public boolean completeRegistration(UserType userType, UUID userUUID) throws SQLException {
        return false;
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
}
