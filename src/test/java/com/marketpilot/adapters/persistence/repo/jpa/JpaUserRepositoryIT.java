package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: use application Spring container for datasource creation (dev/stg/prod datasources)
@DataJpaTest
@Import(JpaUserRepository.class)
class JpaUserRepositoryIT {
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void testSaveUser() {
        Role role = new Role(Role.RoleName.Analyst, Set.of(Permission.VIEW_QUOTE), UserType.CLIENT);
        testEntityManager.persist(role);

        UserFactory userFactory = new UserFactory();
        User testUser = userFactory.createClientUser(Set.of(role), "johnmdoe", "johnmdoe@outlook.com", "John", "M", "Doe");

        jpaUserRepository.save(testUser);

        // TODO: separate tests
        assertEquals(testUser, jpaUserRepository.findByUsername(UserType.CLIENT, testUser.getUsername()).orElse(null));
        assertEquals(testUser, jpaUserRepository.findByUUID(UserType.CLIENT, testUser.getUUID()).orElse(null));
        assertEquals(testUser, jpaUserRepository.findByPersonalEmail(testUser.getClientProfile().getEmail()).orElse(null));
        // TODO: employee user type tests
    }
}