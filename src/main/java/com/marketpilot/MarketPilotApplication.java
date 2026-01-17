package com.marketpilot;

import com.marketpilot.application.ports.auth.TotpService;
import config.AppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MarketPilotApplication {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        ctx.registerShutdownHook();

        TotpService totpService = ctx.getBean(TotpService.class);
        System.out.println(totpService.generateSecret());
    }
}
    