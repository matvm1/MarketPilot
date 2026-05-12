package com.marketpilot.application.ports.auth;

import com.marketpilot.domain.entities.auth.Role;

import java.util.Set;

@Deprecated
public interface RoleCache {
    Set<Role> fetch(Role.RoleName[] roleNames);
    int getId(Role.RoleName roleName);
}
