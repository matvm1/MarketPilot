package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends BaseRepository<Long, Role> {
    Optional<Role> findByRoleName(Role.RoleName roleName);
    Optional<Set<Role>> findByRoleNames(Set<Role.RoleName> roleNames);
    Optional<Set<Role>> getRolesForUser(long userId);
}
