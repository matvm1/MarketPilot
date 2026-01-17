package config;

import com.marketpilot.adapters.persistence.repo.OjdbcRoleRepository;
import com.marketpilot.adapters.persistence.repo.OjdbcUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    public UserRepository userRepository(RoleRepository roleRepository) {
        return new OjdbcUserRepository(roleRepository);
    }

    @Bean
    public RoleRepository roleRepository() {
        return new OjdbcRoleRepository();
    }
}
