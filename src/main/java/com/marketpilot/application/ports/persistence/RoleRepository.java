package com.marketpilot.application.ports.persistence;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByRoleName(Role.RoleName roleName);
}
