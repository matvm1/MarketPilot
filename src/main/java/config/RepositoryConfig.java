package config;

import com.marketpilot.adapters.persistence.repo.jpa.JpaRoleRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcEmployeeRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcPendingVerificationUserRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcRoleRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcUserRepository;
import com.marketpilot.adapters.persistence.repo.springjdbc.SpringJdbcAuthRepository;
import com.marketpilot.application.ports.auth.RoleCache;
import com.marketpilot.domain.repo.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class RepositoryConfig {
    @Bean
    public AuthRepository authRepository(JdbcClient jdbcClient) {
        return new SpringJdbcAuthRepository(jdbcClient);
    }

    @Bean
    public UserRepository userRepository(RoleRepository roleRepository) {
        return new OjdbcUserRepository(roleRepository);
    }

    @Bean
    public RoleRepository roleRepository() {
        return new JpaRoleRepository();
    }

    @Bean
    public PendingVerificationUserRepository pendingVerificationUserRepository(RoleCache roleCache) {
        return new OjdbcPendingVerificationUserRepository(roleCache);
    }

    @Bean
    public EmployeeRepository employeeRepository() {
        return new OjdbcEmployeeRepository();
    }
}
