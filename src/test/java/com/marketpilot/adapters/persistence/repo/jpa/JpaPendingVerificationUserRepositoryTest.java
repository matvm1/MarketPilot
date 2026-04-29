package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.MarketPilotApplication;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.*;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.util.BufferedConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = MarketPilotApplication.class)
@ActiveProfiles("test")
@Import({JpaPendingVerificationUserRepository.class, JpaRoleRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/sql/schema-ddl.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class JpaPendingVerificationUserRepositoryTest {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private PendingVerificationUserRepository pendingVerificationUserRepository;
    @Autowired private RoleRepository roleRepository;

    private UserFactory userFactory;
    private User clientUser;
    private Set<Role> clientRoles;
    private Set<Role> employeeRoles;

    private final byte[] dummyPasswordHash = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");

    @BeforeEach
    public void setUp() {
        roleRepository.save(TestRoles.PERSONAL_INVESTOR_ROLE);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);
        employeeRoles = new HashSet<>();
        employeeRoles.add(TestRoles.ANALYST_ROLE);

        userFactory = new UserFactory();
        clientUser = userFactory.createClientUser(clientRoles, "johnmdoe", "johnmdoe@outlook.com",
                "John", "M", "Doe");
    }

    @Test
    public void registerNewUser_persistsUser() throws SQLException {
        assertTrue(pendingVerificationUserRepository.registerNewUser(UserType.CLIENT, clientUser, clientRoles, dummyPasswordHash, "abc123"));
        Optional<Long> persistedUserId = jdbcClient.sql("SELECT ID FROM APP_USER WHERE USERNAME = :username")
                .param("username", "johnmdoe")
                .query(Long.class)
                .optional();
        assert(persistedUserId.isPresent());
        assertEquals(clientUser.getId(), persistedUserId.get());
        int clientUserRoleCount = jdbcClient.sql("SELECT COUNT(*) FROM APP_USER_ROLE WHERE USER_ID = :userId")
                .param("userId", clientUser.getId())
                .query(Integer.class)
                .single();
        assertEquals(clientRoles.size(), clientUserRoleCount);
    }

    @Test
    public void crossRegister_persistsUser() throws SQLException {
        pendingVerificationUserRepository.registerNewUser(UserType.CLIENT, clientUser, clientRoles, dummyPasswordHash, "abc123");
        pendingVerificationUserRepository.crossRegister(UserType.EMPLOYEE, clientUser, employeeRoles, dummyPasswordHash, "321cba");
    }
}
