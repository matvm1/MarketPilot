package config;

import com.marketpilot.adapters.persistence.repo.*;
import com.marketpilot.domain.repo.EmployeeRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
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

    @Bean
    public PendingVerificationUserRepository pendingVerificationUserRepository(OjdbcRoleCache roleCache) {
        return new OjdbcPendingVerificationUserRepository(roleCache);
    }

    @Bean
    public EmployeeRepository employeeRepository() {
        return new OjdbcEmployeeRepository();
    }
}
