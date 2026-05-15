package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.UserType;
import integration.OracleTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@Import(JpaRoleRepository.class)
@AutoConfigureTestDatabase(replace = NONE)
class JpaRoleRepositoryTest extends OracleTestBase {

    @Autowired
    private JpaRoleRepository roleRepository;

    @Test
    void testSaveRole() {
        Role role = new Role(Role.RoleName.Analyst, Set.of(Permission.PUBLISH_ARTICLE), UserType.EMPLOYEE);
        roleRepository.save(role);

        assertEquals(role, roleRepository.findById(role.getId()).orElse(null));
    }
}