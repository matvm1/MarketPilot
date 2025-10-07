package com.marketpilot.domain.repo;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByRoleName(Role.RoleName roleName);
}
