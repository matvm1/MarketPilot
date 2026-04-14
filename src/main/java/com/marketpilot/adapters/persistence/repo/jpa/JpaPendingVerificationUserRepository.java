package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.util.Tuple;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.SQLException;
import java.util.*;

public class JpaPendingVerificationUserRepository implements PendingVerificationUserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Tuple<User, Map<String, Object>>> findByUsername(UserType userType, String username) {
        String sql = "SELECT * FROM APP_USER WHERE USERNAME = :username AND " +
                (userType == UserType.CLIENT ? "CLIENT_USER_STATUS_ID" : "EMPLOYEE_USER_STATUS_ID") +
                UserStatus.PENDING.getCode();

        Object[] result = (Object[]) entityManager.createNativeQuery(sql, User.class)
                .setParameter("username", username)
                .getSingleResultOrNull();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean registerNewUser(UserType userType, User user, Set<Role> roles, byte[] passwordHash, String verificationCode) throws SQLException {
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
