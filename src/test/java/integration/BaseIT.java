package integration;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.repo.RoleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIT {
    @Autowired private RoleRepository roleRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    protected Role personalInvestorRole;
    protected Role analystRole;

    @BeforeAll
    public void setUp() {
        personalInvestorRole = TestRoles.personalInvestorRole();
        analystRole = TestRoles.analystRole();

        boolean roleResult = Boolean.TRUE.equals(
                transactionTemplate.execute(status -> {
                    return roleRepository.save(personalInvestorRole)
                        && roleRepository.save(analystRole);
        }));
        assertTrue(roleResult);
    }
}