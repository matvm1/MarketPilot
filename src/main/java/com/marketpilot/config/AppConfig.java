package com.marketpilot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({AuthServiceConfig.class,
        RegistrationServiceConfig.class,
        DataAccessConfig.class})
@ComponentScan(basePackages = "com.marketpilot.adapters.persistence.repo")
public class AppConfig {

}
