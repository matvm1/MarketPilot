package config;

import com.marketpilot.adapters.SjmEmailEngine;
import com.marketpilot.adapters.auth.Password4JHasher;
import com.marketpilot.adapters.persistence.repo.OjdbcPendingVerificationUserRepository;
import com.marketpilot.adapters.persistence.repo.OjdbcRoleCache;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.VerificationCodeGenerator;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.RoleCache;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.repo.EmployeeRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.util.SecureRandomVerificationCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrationServiceConfig {
    @Bean
    public RegistrationService RegistrationService(UserRepository userRepository,
                                                   PendingVerificationUserRepository pendingVerificationUserRepository,
                                                   EmployeeRepository employeeRepository,
                                                   EmailEngine emailEngine,
                                                   PasswordHasher passwordHasher,
                                                   UserFactory userFactory,
                                                   RoleCache roleCache,
                                                   VerificationCodeGenerator verificationCodeGenerator) {
        return new RegistrationService(userRepository, pendingVerificationUserRepository, employeeRepository, emailEngine, passwordHasher, userFactory,
                roleCache, verificationCodeGenerator);
    }

    @Bean
    public SjmEmailEngine emailEngine() {
        return SjmEmailEngine.getInstance();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new Password4JHasher();
    }

    // TODO: Consider moving to another config file (perhaps one is needed for domain objects)
    @Bean
    public UserFactory userFactory() {
        return new UserFactory();
    }

    @Bean
    public RoleCache roleCache(RoleRepository roleRepository) {
        return new OjdbcRoleCache(roleRepository);
    }

    @Bean
    public VerificationCodeGenerator verificationCodeGenerator() {
        return new SecureRandomVerificationCodeGenerator();
    }
}
