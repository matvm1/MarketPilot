package integration.infrastructure;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.oracle.OracleContainer;

import java.time.Duration;

// mvn install/test:
// -Dtest.env=local                  -> in-mem H2 w/Oracle dialect
// -Dtest.env=local -Dtest.db=oracle -> shared oracle-free testcontainer
// -Dtest.env=ci                     -> in-mem H2 w/Oracle dialect
// -Dtest.env=integration            -> shared oracle-free testcontainer
public class OracleContainerContextCustomizer implements ContextCustomizer {
    private static final String ENV = System.getProperty("test.env", "local");
    // TODO: couple default mode with ENV, or remove default and throw exception if improper configuration
    private static final String MODE = System.getProperty("test.db", "h2oracle");

    private static final OracleContainer oracle;

    // TODO: disposable container per test for higher fidelity integration/stage tier builds
    static {
        if (MODE.equalsIgnoreCase("oracle") || ENV.equalsIgnoreCase("integration")) {
            oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
                .withDatabaseName("marketpilot")
                .withUsername("marketpilot")
                .withPassword("marketpilot")
                .withStartupTimeout(Duration.ofMinutes(3))
                .withInitScript("sql/schema-ddl.sql");
            oracle.start();
        }
        else {
            oracle = null;
        }
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        if (oracle != null && oracle.isRunning()) {
            TestPropertyValues.of(
                "spring.datasource.url=" + oracle.getJdbcUrl(),
                "spring.datasource.username=" + oracle.getUsername(),
               "spring.datasource.password=" + oracle.getPassword()
            ).applyTo(context);
        }
        else if (oracle == null &&
                (MODE.equalsIgnoreCase("h2oracle") ||
                ENV.equalsIgnoreCase("local") ||
                ENV.equalsIgnoreCase("ci"))
        ) {
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.jpa.database-platform=org.hibernate.dialect.OracleDialect",
                    "spring.sql.init.mode=always",
                    "spring.sql.init.schema-locations=classpath:sql/schema-ddl.sql"
            ).applyTo(context);
        }
    }
}
