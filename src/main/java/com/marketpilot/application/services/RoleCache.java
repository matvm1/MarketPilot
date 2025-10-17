package com.marketpilot.application.services;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.repo.RoleRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleCache {
    private final RoleRepository roleRepository;
    private Map<Role.RoleName, Role> cache;

    public RoleCache(RoleRepository roleRepository) {
        if (roleRepository == null)
            throw new IllegalArgumentException("roleRepository cannot be null");
        this.roleRepository = roleRepository;
    }

    public void load() {
        try {
            Set<Role.RoleName> roleNameSet = Arrays.stream(Role.RoleName.values()).collect(Collectors.toSet());
            Set<Role> roles = roleRepository.findByRoleNames(roleNameSet).orElseThrow();
            cache = roles.stream().collect(Collectors.toMap(Role::getRoleName, role -> role));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Role> fetch(Role.RoleName[] roleNames) {
        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : roleNames)
            roles.add(cache.get(roleName));
        return roles;
    }
}
