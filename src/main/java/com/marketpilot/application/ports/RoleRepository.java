package com.marketpilot.application.ports;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByRoleName(Role.RoleName roleName);
}
