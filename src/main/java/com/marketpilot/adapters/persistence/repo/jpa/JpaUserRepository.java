package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.domain.entities.auth.MfaType;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class JpaUserRepository implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUUID(UserType userType, UUID uuid) {
        return findBy(userType, "uuid", uuid);
    }

    @Override
    public Optional<User> findByUsername(UserType userType, String username) {
        return findBy(userType, "username", username);
    }

    @Override
    public Optional<User> findByEmployeeId(String employeeId) {
        return findBy(UserType.EMPLOYEE, "employeeId", employeeId);
    }

    @Override
    public Optional<User> findByPersonalEmail(String email) {
        return findBy(UserType.CLIENT, "email", email);
    }

    @Override
    public Optional<User> findByEmployeeEmail(String email) {
        return findBy(UserType.EMPLOYEE, "email", email);
    }

    private Optional<User> findBy(UserType userType, String propertyName, Object property) {
        if (property == null || ((property instanceof String) && ((String)property).isBlank()))
            return Optional.empty();

        int filteringEntity = userType == UserType.CLIENT && propertyName.equals("email") ? 1
            : (propertyName.equals("employeeId") || propertyName.equals("email")) ? 2
            : 0;

        String jpql = "SELECT u FROM User u " +
            (filteringEntity == 1 ? "JOIN u.clientProfile c " : filteringEntity == 2 ? "JOIN u.employeeProfile e " : "") +
            "WHERE " + (filteringEntity == 0 ? "u" : filteringEntity == 1 ? "c" : "e") + "." + propertyName + " = :" + propertyName +  " AND " +
            (userType == UserType.CLIENT ? "u.clientProfile IS NOT NULL" : "u.employeeProfile IS NOT NULL");

        return entityManager.createQuery(jpql, User.class)
            .setParameter(propertyName, property)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
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
        if (entity == null)
            return false;

        entityManager.persist(entity);
        return true;
    }

    @Override
    public int count() {
        return 0;
    }
}
