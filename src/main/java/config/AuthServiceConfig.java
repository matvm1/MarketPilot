package config;

import com.marketpilot.adapters.auth.JavaTotpService;
import com.marketpilot.application.ports.auth.TotpService;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceConfig {
    @Bean
    public TotpService totpService() {
        return new JavaTotpService(new SystemTimeProvider(), HashingAlgorithm.SHA256);
    }
}
