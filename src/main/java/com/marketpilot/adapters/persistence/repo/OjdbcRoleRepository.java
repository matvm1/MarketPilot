package com.marketpilot.adapters.persistence.repo;

import com.marketpilot.adapters.persistence.jdbc.JdbcExecutor;
import com.marketpilot.adapters.persistence.jdbc.Param;
import com.marketpilot.util.Tuple;
import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.RoleRepository;

import java.util.*;

import static com.marketpilot.adapters.persistence.jdbc.Param.longP;
import static com.marketpilot.adapters.persistence.jdbc.Param.stringP;

public class OjdbcRoleRepository implements RoleRepository {

    @Override
    public Optional<Role> findByRoleName(RoleName roleName) {
        if (roleName == null)
            return Optional.empty();
        String roleNameStr = roleName.toString();
        Optional<Set<Permission>> permissionsOptional = JdbcExecutor.fetchToSet("""
                    SELECT P.NAME AS PERMISSION_NAME
                    FROM APP_ROLE R
                    JOIN APP_ROLE_PERMISSION ARP ON R.ID = ARP.ROLE_ID
                    JOIN APP_PERMISSION P ON P.ID = ARP.PERMISSION_ID
                    WHERE R.NAME = ?
                """,
                rs -> Permission.valueOf(rs.getString("PERMISSION_NAME")),
                stringP(1, roleNameStr));
        if (permissionsOptional.isPresent()) {
            Optional<UserType> userTypeOptional = JdbcExecutor.fetchRecord("""
                        SELECT UT.NAME AS USER_TYPE_NAME
                        FROM APP_ROLE R
                        JOIN APP_USER_TYPE UT ON R.USER_TYPE_ID = UT.ID
                        WHERE R.NAME = ?
                    """,
                    rs -> UserType.valueOf(rs.getString("USER_TYPE_NAME")),
            stringP(1, roleNameStr));
            if (userTypeOptional.isPresent())
                return Optional.of(new Role(roleName, permissionsOptional.get(), userTypeOptional.get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Set<Role>> findByRoleNames(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty())
            return Optional.empty();
        StringBuilder binders = new StringBuilder("(");
        Param[] params = new Param[roleNames.size()];
        int i = 0;
        for (RoleName roleName : roleNames) {
            if (i == roleNames.size() - 1)
                binders.append("?)");
            else
                binders.append("?, ");
            params[i] = stringP(++i, roleName.toString());
        }
        Optional<Map<Long, Set<Permission>>> permissionsMapOptional = JdbcExecutor.fetchToMultiMap(String.format("""
                    SELECT R.ID AS ROLE_ID, P.NAME AS PERMISSION_NAME
                    FROM APP_ROLE R
                    JOIN APP_ROLE_PERMISSION ARP ON R.ID = ARP.ROLE_ID
                    JOIN APP_PERMISSION P ON P.ID = ARP.PERMISSION_ID
                    WHERE R.NAME IN %s
                    """, binders),
                rs -> rs.getLong("ROLE_ID"),
                rs -> Permission.valueOf(rs.getString("PERMISSION_NAME")),
                params);

        if (permissionsMapOptional.isPresent()) {
            Optional<Map<Long, Tuple<RoleName, UserType>>> userTypeMapOptional = JdbcExecutor.fetchToMap(String.format("""
                        SELECT R.ID AS ROLE_ID, R.NAME AS ROLE_NAME, UT.NAME AS USER_TYPE_NAME
                        FROM APP_ROLE R
                        JOIN APP_USER_TYPE UT ON R.USER_TYPE_ID = UT.ID
                        WHERE R.NAME IN %s
                    """, binders),
                    rs -> rs.getLong("ROLE_ID"),
                    rs -> new Tuple<>(
                            RoleName.valueOf(rs.getString("ROLE_NAME")),
                            UserType.valueOf(rs.getString("USER_TYPE_NAME"))
                    ),
                    params);
            if (userTypeMapOptional.isPresent()) {
                Map<Long, Tuple<RoleName, UserType>> userTypeMap = userTypeMapOptional.get();
                Set<Role> roles = new HashSet<>();
                for (Map.Entry<Long, Set<Permission>> rolePermissionEntry : permissionsMapOptional.get().entrySet()) {
                    Tuple<RoleName, UserType> roleAttributes = userTypeMap.get(rolePermissionEntry.getKey());
                    roles.add(new Role(roleAttributes.t(), rolePermissionEntry.getValue(), roleAttributes.u()));
                }
                return Optional.of(roles);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Set<Role>> getRolesForUser(long userId) {
       // Optional<List<DataRecord<RoleName>>>
        Optional<Map<Long, Set<Permission>>> permissionsOptional = JdbcExecutor.fetchToMultiMap(
            """
                    SELECT ARP.ROLE_ID AS ROLE_ID, P.NAME AS PERMISSION_NAME
                    FROM APP_USER U
                    JOIN APP_USER_ROLE AUR ON U.ID = ? AND U.ID = AUR.USER_ID
                    JOIN APP_ROLE_PERMISSION ARP ON ARP.ROLE_ID = AUR.ROLE_ID
                    JOIN APP_PERMISSION P ON ARP.PERMISSION_ID = P.ID
               """,
            rs -> rs.getLong("ROLE_ID"),
            rs -> Permission.valueOf(rs.getString("PERMISSION_NAME")),
            longP(1, userId));

        if (permissionsOptional.isPresent()) {
            Map<Long, Set<Permission>> rolePermissions = permissionsOptional.get();
            Optional<Map<Long, Tuple<RoleName, UserType>>> roleAttributesOptional =
                JdbcExecutor.fetchToMap("""
                    SELECT R.ID AS ROLE_ID, R.NAME AS ROLE_NAME, AUT.NAME AS USER_TYPE
                    FROM APP_USER U
                    JOIN APP_USER_ROLE AUR ON U.ID = ? AND U.ID = AUR.USER_ID
                    JOIN APP_ROLE R ON R.ID = AUR.ROLE_ID
                    JOIN APP_USER_TYPE AUT ON AUT.ID = R.USER_TYPE_ID
                    """, rs -> rs.getLong("ROLE_ID"),
                    rs -> new Tuple<>(
                            RoleName.valueOf(rs.getString("ROLE_NAME")),
                            UserType.valueOf(rs.getString("USER_TYPE"))
                    ),
                longP(1, userId));
            if (roleAttributesOptional.isPresent()) {
                Map<Long, Tuple<RoleName, UserType>> roleAttributes = roleAttributesOptional.get();

                Set<Role> userRoles = new HashSet<>();
                for (Map.Entry<Long, Tuple<RoleName, UserType>> attributeEntry : roleAttributes.entrySet()) {
                    Long roleId = attributeEntry.getKey();
                    Tuple<RoleName, UserType> attributes = attributeEntry.getValue();
                    Set<Permission> permissions = rolePermissions.getOrDefault(roleId, Set.of());
                    userRoles.add(new Role(attributes.t(), permissions, attributes.u()));
                }
                return Optional.of(userRoles);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Role> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public boolean save(Role entity) {
        return false;
    }

    @Override
    public int count() {
        return 0;
    }
}
