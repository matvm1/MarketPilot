package config;

import org.springframework.context.annotation.Import;

@Import({AuthServiceConfig.class, RepositoryConfig.class})
public class AppConfig {
    
}
