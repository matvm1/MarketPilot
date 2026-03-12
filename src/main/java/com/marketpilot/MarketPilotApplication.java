package com.marketpilot;

import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.entities.auth.profile.ClientProfile;
import com.marketpilot.util.BufferedConverter;
import config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
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
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource.getConnection().getMetaData());

        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");
        EntityManager em = emf.createEntityManager();

        emf.getMetamodel().getEntities()
                .forEach(entityType -> System.out.println(entityType.getName()));

        testJpaPersistence(em);
        testPersistedEntity(dataSource);

        Thread.currentThread().join();
    }

    private static void testJpaPersistence(EntityManager em) {
        final Set<Permission> AUTHENTICATED_BASE_PERMISSIONS = Set.of(
                Permission.VIEW_QUOTE,
                Permission.VIEW_ARTICLE,
                Permission.VIEW_SECURITY_RATING,
                Permission.CREATE_WATCHLIST,
                Permission.DELETE_WATCHLIST
        );

        final Set<Permission> INVESTOR_TRANSACTION_PERMISSIONS = Set.of(
                Permission.CREATE_BROKERAGE_ACCOUNT,
                Permission.CLOSE_BROKERAGE_ACCOUNT,
                Permission.LINK_BROKERAGE_ACCOUNT_TO_EXTERNAL,
                Permission.TRANSFER_FUNDS,
                Permission.PLACE_TRADE,
                Permission.VIEW_PORTFOLIO
        );

        final Role PERSONAL_INVESTOR_ROLE = new Role(
                Role.RoleName.Analyst,
                Collections.unmodifiableSet(new HashSet<>() {{
                    addAll(AUTHENTICATED_BASE_PERMISSIONS);
                    addAll(INVESTOR_TRANSACTION_PERMISSIONS);
                }}),
                UserType.CLIENT
        );

        UserFactory userFactory = new UserFactory();
        User user = userFactory.createClientUser(Set.of(PERSONAL_INVESTOR_ROLE), "johnmdoe", "johnmdoe@outlook.com", "John", "M", "Doe");

        em.getTransaction().begin();
        try {
            em.persist(PERSONAL_INVESTOR_ROLE);
            em.persist(user);
            em.getTransaction().commit();
            System.out.println("Persisted with id: " + PERSONAL_INVESTOR_ROLE.getId());
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static void testPersistedEntity(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Map<String,Object>> rows =
                jdbcTemplate.queryForList("SELECT * FROM CLIENT_PROFILE");

        rows.forEach(System.out::println);
    }
}
    