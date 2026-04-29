package config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AuthServiceConfig {
    @Bean
    public AuthenticationService authenticationService(AuthRepository authRepository, UserRepository userRepository, TwoFactorService totpService,
                                                       PasswordHasher passwordHasher, SessionManager sessionManager) {
        return new AuthenticationService(authRepository, userRepository, totpService, passwordHasher, sessionManager);
    }

    @Bean
    public TotpService totpService() {
        return new JavaTotpService(new SystemTimeProvider(), HashingAlgorithm.SHA256);
    }

    //TODO
    @Bean
    public SessionManager sessionManager() {
        return new SessionManager() {
            @Override
            public Optional<UserSession> createSession(AuthenticationContext authenticationContext) {
                return Optional.empty();
            }

            @Override
            public Optional<UserSession> getSession(int sessionId) {
                return Optional.empty();
            }

            @Override
            public void invalidate(int sessionId) {

            }
        };
    }
}
