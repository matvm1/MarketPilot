package config;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DataAccessConfig {
    @Bean
    DataSource dataSource() {
        try {
            String propsPath = System.getenv("DB_PROPERTIES_PATH");
            if (propsPath == null) {
                System.err.println("DB_PROPERTIES_PATH environment variable is not set");
                return null;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(propsPath)) {
                props.load(fis);
            } catch (IOException e) {
                System.out.println("File " + propsPath + " could not be found.");
            }

            PoolDataSource poolDataSource;
            poolDataSource = PoolDataSourceFactory.getPoolDataSource();
            poolDataSource.setConnectionFactoryClassName("oracle.jdbc.replay.OracleConnectionPoolDataSourceImpl");

            poolDataSource.setURL("jdbc:oracle:thin:@marketpilotdev_high");

            poolDataSource.setUser(props.getProperty("db.user"));
            poolDataSource.setPassword(props.getProperty("db.password"));
            poolDataSource.setConnectionPoolName("JDBC_UCP_POOL");

            return poolDataSource;
        }
        catch (SQLException e) {
            //TODO: Retry & log
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize UCP pool\n");
        }
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
        lcemfb.setDataSource(dataSource);
        lcemfb.setPackagesToScan("com.marketpilot.domain");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.ORACLE);
        lcemfb.setJpaVendorAdapter(vendorAdapter);

        return lcemfb;
    }
}
