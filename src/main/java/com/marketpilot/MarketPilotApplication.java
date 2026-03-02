package com.marketpilot;

import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.util.BufferedConverter;
import config.AppConfig;
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
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        ctx.registerShutdownHook();

        /*AuthenticationService authenticationService = ctx.getBean(AuthenticationService.class);
        System.out.println(authenticationService.initiateClientAuthentication("abc", null, null));

        RegistrationService registrationService = ctx.getBean(RegistrationService.class);
        System.out.println(registrationService.initiateClientRegistration("user", null, null,
                "user@marketpilot.com", "user", "", "1"));
         */
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource.getConnection().getMetaData());

        Arrays.stream(ctx.getBeanDefinitionNames())
                .sorted()
                .forEach(System.out::println);

        Thread.currentThread().join();
    }
}
    