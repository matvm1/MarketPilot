package com.marketpilot;

import config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.*;

@SpringBootApplication
public class MarketPilotApplication {
    public static void main(String[] args) throws InterruptedException {
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
    