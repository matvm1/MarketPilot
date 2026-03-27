package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class JpaUserRepository implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUUID(UserType userType, UUID uuid) {
        if (userType == null || uuid == null)
            return Optional.empty();

        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.uuid = :uuid AND " +
                                (userType == UserType.CLIENT
                                        ? "u.clientProfile IS NOT NULL"
                                        : "u.employeeProfile IS NOT NULL"),
                        User.class)
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(UserType userType, String username) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmployeeId(String employeeId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByPersonalEmail(String personalEmail) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmployeeEmail(String employeeEmail) {
        return Optional.empty();
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
        return Optional.empty();
    }

    @Override
    public Optional<byte[]> getEmployeePasswordHash(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<char[]> getClientTotpSecret(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<char[]> getEmployeeTotpSecret(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<MfaType> getMfaType(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<Properties> getAuthProperties(UserType userType, UUID uuid) throws SQLException {
        return Optional.empty();
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
