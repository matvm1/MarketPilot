package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.repo.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// TODO: Implementations for several methods
@Repository
public class JpaRoleRepository implements RoleRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Role> findByRoleName(Role.RoleName roleName) {
        return Optional.empty();
    }

    @Override
    public Optional<Set<Role>> findByRoleNames(Set<Role.RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty())
            return Optional.empty();

        Set<Role> result = new HashSet<>(entityManager.createQuery("SELECT r FROM Role r WHERE r.roleName IN :roleNames", Role.class)
                .setParameter("roleNames", roleNames)
                .getResultList());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    @Override
    public Optional<Set<Role>> getRolesForUser(long userId) {
        Set<Role> result = new HashSet<>(entityManager.createQuery("SELECT r FROM User u JOIN u.roles r WHERE u.id = :userId", Role.class)
                .setParameter("userId", userId)
                .getResultList());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        if (roleId < 0)
            return Optional.empty();

        return entityManager.createQuery("SELECT r FROM Role r WHERE r.id = :roleId", Role.class)
                .setParameter("roleId", roleId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public boolean save(Role entity) {
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
