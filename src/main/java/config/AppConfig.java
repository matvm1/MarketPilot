package config;

import org.springframework.context.annotation.Import;

@Import({AuthServiceConfig.class,
        RegistrationServiceConfig.class,
        RepositoryConfig.class})
public class AppConfig {
    
}
