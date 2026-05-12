package com.marketpilot.config;

import com.marketpilot.adapters.auth.JavaTotpService;
import com.marketpilot.adapters.auth.Password4JHasher;
import com.marketpilot.application.dto.auth.AuthenticationContext;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.ports.auth.TotpService;
import com.marketpilot.application.ports.auth.TwoFactorService;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.UserSession;
import com.marketpilot.domain.repo.AuthRepository;
import com.marketpilot.domain.repo.UserRepository;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AuthServiceConfig {
    @Bean
    public AuthenticationService authenticationService(AuthRepository authRepository, UserRepository userRepository, TwoFactorService totpService,
                                                       PasswordHasher passwordHasher) {
        return new AuthenticationService(authRepository, userRepository, totpService, passwordHasher);
    }

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    @Bean
    public TotpService totpService(TimeProvider timeProvider, @Value("${totp.hashing-algorithm}") HashingAlgorithm hashingAlgorithm) {
        return new JavaTotpService(timeProvider, hashingAlgorithm);
    }
}
