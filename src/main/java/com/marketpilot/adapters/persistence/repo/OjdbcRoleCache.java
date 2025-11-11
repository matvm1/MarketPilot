package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.application.ports.auth.RoleCache;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.repo.RoleRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OjdbcRoleCache implements RoleCache {
    private static OjdbcRoleCache instance;
    private RoleRepository roleRepository;
    private Map<Role.RoleName, Role> roleCache;
    private Map<Role.RoleName, Integer> idCache;

    private OjdbcRoleCache() {
        this.roleRepository = new OjdbcRoleRepository();
    }

    public static OjdbcRoleCache getInstance() {
        if (instance == null) {
            instance = new OjdbcRoleCache();
            instance.load();
        }
        return instance;
    }

    private void load() {
        try {
            Set<Role.RoleName> roleNameSet = Arrays.stream(Role.RoleName.values()).collect(Collectors.toSet());
            Set<Role> roles = roleRepository.findByRoleNames(roleNameSet).orElseThrow();
            System.out.println(roles);
            roleCache = roles.stream().collect(Collectors.toMap(Role::getRoleName, role -> role));
            idCache = JdbcExecutor.fetchToMap("""
                SELECT ID, NAME
                FROM APP_ROLE
                """,
                    rs -> Role.RoleName.valueOf(rs.getString("NAME")),
                    rs -> rs.getInt("ID")
            ).orElseThrow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Role> fetch(Role.RoleName[] roleNames) {
        Set<Role> roles = new HashSet<>();
        for (Role.RoleName roleName : roleNames)
            roles.add(roleCache.get(roleName));
        return roles;
    }

    public int getId(Role.RoleName roleName) {
        return idCache.get(roleName);
    }
}
