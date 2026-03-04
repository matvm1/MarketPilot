package com.marketpilot;

import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.util.BufferedConverter;
import config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;

@SpringBootApplication
public class MarketPilotApplication {
    public static void main(String[] args) throws InterruptedException, SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles("dev");
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
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource.getConnection().getMetaData());

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();

        emf.getMetamodel().getEntities()
                .forEach(entityType -> System.out.println(entityType.getName()));

        try {
            System.out.println(em.createNativeQuery("SELECT * FROM APP_ROLE").getResultList());
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        Thread.currentThread().join();
    }
}
    