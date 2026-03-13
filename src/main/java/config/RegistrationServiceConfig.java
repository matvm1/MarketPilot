package config;

import com.marketpilot.adapters.PebbleHtmlTemplateEngine;
import com.marketpilot.adapters.SjmEmailEngine;
import com.marketpilot.adapters.auth.Password4JHasher;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcRoleCache;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.HtmlTemplateEngine;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class RegistrationServiceConfig {
    @Bean
    public RegistrationService registrationService(UserRepository userRepository,
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
    public EmailEngine emailEngine(HtmlTemplateEngine htmlTemplateEngine) {
        String propsPath = System.getenv("SMTP_PROPERTIES_PATH");
        if (propsPath == null) {
            throw new IllegalArgumentException("SMTP_PROPERTIES_PATH environment variable is not set");
        }

        Properties props = new Properties();
        try(FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new SjmEmailEngine(htmlTemplateEngine,
                props.getProperty("SMTP_HOST"),
                props.getProperty("SMTP_EMAIL"),
                props.getProperty("SMTP_PASSWORD"));
    }

    @Bean
    public HtmlTemplateEngine htmlTemplateEngine() {
        return new PebbleHtmlTemplateEngine();
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
