package com.marketpilot.adapters.persistence.repo.jpa;

import com.marketpilot.MarketPilotApplication;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.*;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.util.BufferedConverter;
import config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

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
public class JpaPendingVerificationUserRepositoryTest {
    @Autowired private JdbcClient jdbcClient;
    @Autowired private PendingVerificationUserRepository pendingVerificationUserRepository;
    @Autowired private RoleRepository roleRepository;

    private UserFactory userFactory;
    private User clientUser;
    private Set<Role> clientRoles;

    private final byte[] dummyPasswordHash = BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");

    private String schemaDdl = """
                CREATE TABLE APP_USER_AUTH (
                    USER_ID        NUMBER PRIMARY KEY,
                    IS_CLIENT    BOOLEAN DEFAULT FALSE NOT NULL,
                    CLIENT_PASSWORD_HASH RAW(200) NULL,
                    CLIENT_REGISTRATION_CODE       VARCHAR2(16),
                    CLIENT_REGISTRATION_EXPIRATION TIMESTAMP NULL,
                    CLIENT_TOTP_SECRET             VARCHAR2(64),
                    CLIENT_USER_STATUS_ID          NUMBER(2),
                    IS_EMPLOYEE  BOOLEAN DEFAULT FALSE NOT NULL,
                    EMPLOYEE_PASSWORD_HASH RAW(200) NULL,
                    EMPLOYEE_REGISTRATION_CODE       VARCHAR2(16),
                    EMPLOYEE_REGISTRATION_EXPIRATION TIMESTAMP NULL,
                    EMPLOYEE_TOTP_SECRET             VARCHAR2(64),
                    EMPLOYEE_USER_STATUS_ID          NUMBER(2),
                
                    CONSTRAINT FK_APP_USER_AUTH FOREIGN KEY (USER_ID) REFERENCES APP_USER(ID)
                );
                """;

    @BeforeEach
    public void setUp() {
        jdbcClient.sql(schemaDdl).update();

        roleRepository.save(TestRoles.PERSONAL_INVESTOR_ROLE);

        clientRoles = new HashSet<>();
        clientRoles.add(TestRoles.PERSONAL_INVESTOR_ROLE);

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
}
