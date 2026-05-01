package com.marketpilot;

import com.marketpilot.adapters.persistence.repo.jpa.JpaPendingVerificationUserRepository;
import com.marketpilot.application.dto.auth.UserStatus;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.entities.auth.profile.ClientProfile;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

@SpringBootApplication
public class MarketPilotApplication {
    public static void main(String[] args) throws InterruptedException, SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles("test");
        ctx.register(AppConfig.class);
        ctx.refresh();
        ctx.registerShutdownHook();

        Arrays.stream(ctx.getBeanDefinitionNames())
                .sorted()
                .forEach(System.out::println);

        /*AuthenticationService authenticationService = ctx.getBean(AuthenticationService.class);
        System.out.println(authenticationService.initiateClientAuthentication("abc", null, null));

        RegistrationService registrationService = ctx.getBean(RegistrationService.class);
        System.out.println(registrationService.initiateClientRegistration("user", null, null,
                "user@marketpilot.com", "user", "", "1"));
         */

        Thread.currentThread().join();
    }
}
    