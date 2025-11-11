package com.marketpilot.application.ports.auth;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Set;

public interface RoleCache {
    public Set<Role> fetch(Role.RoleName[] roleNames);
    public int getId(Role.RoleName roleName);
}
