package integration.infrastructure;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.oracle.OracleContainer;

import java.time.Duration;

public class OracleContainerContextCustomizer implements ContextCustomizer {
    private static final OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withDatabaseName("marketpilot")
            .withUsername("marketpilot")
            .withPassword("marketpilot")
            .withStartupTimeout(Duration.ofMinutes(3))
            .withInitScript("sql/schema-ddl.sql");

    static {
        oracle.start();
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        TestPropertyValues.of(
            "spring.datasource.url=" + oracle.getJdbcUrl(),
            "spring.datasource.username=" + oracle.getUsername(),
            "spring.datasource.password=" + oracle.getPassword()
        )
        .applyTo(context);
    }
}
