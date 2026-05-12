package integration;

import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.MfaType;
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
    @Autowired private TotpService totpService;

    protected User clientUser;
    protected User employeeUser;

    @BeforeAll
    public void setUp() {
        super.setUp();

        UserFactory userFactory = new UserFactory();
        clientUser = userFactory.createClientUser(Set.of(personalInvestorRole), "quinnj", "quinnjordan@personal.com",
                "Quinn", "A", "Jordan");
        employeeUser = userFactory.createEmployeeUser("ab123456", Set.of(analystRole), "amorgan", "amorgan@company.com",
                "Alex", "A", "Morgan");

        transactionTemplate.execute(status -> {
            boolean persistenceResult = userRepository.save(clientUser) && userRepository.save(employeeUser);
            entityManager.flush();

            if (!persistenceResult)
                throw new IllegalArgumentException("Failed to persist test users with userRepository");

            String authSql = """
                    INSERT INTO APP_USER_AUTH (
                        USER_ID,
                        UUID,
                        MFATYPE_ID,
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
                        :mfaTypeId,
                        :isClient,
                        :clientPasswordHash,
                        '0p9o8i7u',
                        NULL,
                        :clientTotpSecret,
                        :clientUserStatusId,
                        :isEmp,
                        :empPasswordHash,
                        '0p9o8i7u',
                        NULL,
                        :empTotpSecret,
                        :empUserStatusId
                    )""";

            int jdbcResult1 = jdbcClient.sql(authSql)
                    .param("userId", clientUser.getId())
                    .param("uuid", clientUser.getUUID())
                    .param("mfaTypeId", MfaType.TOTP)
                    .param("isClient", true)
                    .param("clientPasswordHash", passwordHasher.hash(TestAuthProperties.dummyPassword()))
                    .param("clientTotpSecret", totpService.generateSecret())
                    .param("clientUserStatusId", UserStatus.ACTIVE.getCode())
                    .param("isEmp", false)
                    .param("empPasswordHash", null)
                    .param("empTotpSecret", null)
                    .param("empUserStatusId", null)
                    .update();

            int jdbcResult2 = jdbcClient.sql(authSql)
                    .param("userId", employeeUser.getId())
                    .param("uuid", employeeUser.getUUID())
                    .param("mfaTypeId", MfaType.TOTP)
                    .param("isClient", false)
                    .param("clientPasswordHash", null)
                    .param("clientTotpSecret", null)
                    .param("clientUserStatusId", null)
                    .param("isEmp", true)
                    .param("empPasswordHash", passwordHasher.hash(TestAuthProperties.dummyPassword()))
                    .param("empTotpSecret", totpService.generateSecret())
                    .param("empUserStatusId", UserStatus.ACTIVE.getCode())
                    .update();

            if (jdbcResult1 != 1 || jdbcResult2 != 1)
                throw new IllegalStateException("Failed to persist test user authentication properties with jdbcClient");

            return null;
        });
    }
}
