package integration;

import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.repo.UserRepository;
import jakarta.persistence.EntityManager;
import objects.TestAuthProperties;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

public class RegistrationFixtureIT extends BaseFixtureIT {
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcClient jdbcClient;
    @Autowired private UserRepository userRepository;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private PasswordHasher passwordHasher;

    protected User clientUser;
    protected User employeeUser;

    @BeforeAll
    public void setUp() {
        super.setUp();

        UserFactory userFactory = new UserFactory();
        clientUser = userFactory.createClientUser(Set.of(personalInvestorRole), "quinnj", "quinnjordan@personal.com",
                "Quinn", "A", "Jordan");
        employeeUser = userFactory.createEmployeeUser("ab123456", Set.of(analystRole), "quinnj", "quinnjordan@company.com",
                "Quinn", "A", "Jordan");

        transactionTemplate.execute(status -> {
            boolean persistenceResult = userRepository.save(clientUser) && userRepository.save(employeeUser);
            entityManager.flush();

            if (!persistenceResult)
                throw new IllegalArgumentException("Failed to persist test users with userRepository");

            String authSql = """
                    INSERT INTO APP_USER_AUTH (
                        USER_ID,
                        UUID,
                        IS_CLIENT,
                        CLIENT_PASSWORD_HASH,
                        CLIENT_REGISTRATION_CODE,
                        CLIENT_REGISTRATION_EXPIRATION,
                        CLIENT_TOTP_SECRET,
                        CLIENT_USER_STATUS_ID,
                        IS_EMPLOYEE,
                        EMPLOYEE_PASSWORD_HASH,
                        EMPLOYEE_REGISTRATION_CODE,
                        EMPLOYEE_REGISTRATION_EXPIRATION,
                        EMPLOYEE_TOTP_SECRET,
                        EMPLOYEE_USER_STATUS_ID
                    ) VALUES (
                        :userId,
                        :uuid,
                        TRUE,
                        :clientPasswordHash,
                        '0p9o8i7u',
                        NULL,
                        :clientTotpSecret,
                        :userStatusId,
                        FALSE,
                        NULL,
                        NULL,
                        NULL,
                        NULL,
                        NULL
                    )""";

            int jdbcResult = jdbcClient.sql(authSql)
                    .param("userId", clientUser.getId())
                    .param("uuid", clientUser.getUUID())
                    .param("clientPasswordHash", passwordHasher.hash(TestAuthProperties.dummyPassword()))
                    .param("clientTotpSecret", TestAuthProperties.totpSecret())
                    .param("userStatusId", UserStatus.ACTIVE.getCode())
                    .update();

            if (jdbcResult != 1)
                throw new IllegalStateException("Failed to persist test user authentication properties with jdbcClient");

            return null;
        });
    }
}
