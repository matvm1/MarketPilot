package integration.infrastructure;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.oracle.OracleContainer;

import java.time.Duration;

public class OracleContainerContextCustomizer implements ContextCustomizer {
    private static final String MODE = System.getProperty("test.db", "h2");

    private static final OracleContainer oracle;

    static {
        if (MODE.equalsIgnoreCase("oracle")) {
            oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
                .withDatabaseName("marketpilot")
                .withUsername("marketpilot")
                .withPassword("marketpilot")
                .withStartupTimeout(Duration.ofMinutes(3));
            oracle.start();
        }
        else {
            oracle = null;
        }
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        // TODO: prevent schema-ddl from running multiple times
        if (oracle != null && oracle.isRunning()) {
            TestPropertyValues.of(
                "spring.datasource.url=" + oracle.getJdbcUrl(),
                "spring.datasource.username=" + oracle.getUsername(),
               "spring.datasource.password=" + oracle.getPassword()
            )
            .applyTo(context);
        }
        // TODO: enable local/PR/integration builds
        else if (MODE.equalsIgnoreCase("h2oracle")) {
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.jpa.database-platform=org.hibernate.dialect.OracleDialect"
            ).applyTo(context);
        }
    }
}
