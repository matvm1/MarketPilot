package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.UserType;
import integration.infrastructure.JpaDatabaseFidelityTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JpaDatabaseFidelityTest
class JpaRoleRepositoryTest {

    @Autowired
    private JpaRoleRepository roleRepository;

    @Test
    void testSaveRole() {
        Role role = new Role(Role.RoleName.Analyst, Set.of(Permission.PUBLISH_ARTICLE), UserType.EMPLOYEE);
        roleRepository.save(role);

        assertEquals(role, roleRepository.findById(role.getId()).orElse(null));
    }
}