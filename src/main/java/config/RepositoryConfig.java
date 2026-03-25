package config;

import com.marketpilot.adapters.persistence.repo.jpa.JpaRoleRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcEmployeeRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcPendingVerificationUserRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcRoleRepository;
import com.marketpilot.adapters.persistence.repo.ojdbc.OjdbcUserRepository;
import com.marketpilot.application.ports.auth.RoleCache;
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
