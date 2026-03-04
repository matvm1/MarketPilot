package config;

import jakarta.persistence.EntityManagerFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

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

    private LocalContainerEntityManagerFactoryBean buildEMF(
            DataSource dataSource,
            String packagesToScan,
            String persistenceUnitName,
            String ddlAuto,
            boolean showSql) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(packagesToScan);
        em.setPersistenceUnitName(persistenceUnitName);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.ORACLE);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", ddlAuto);
        props.put("hibernate.show_sql", showSql);
        props.put("hibernate.format_sql", showSql);
        em.setJpaProperties(props);

        return em;
    }

    @Bean(name = "entityManagerFactory")
    @Profile("dev")
    LocalContainerEntityManagerFactoryBean emfDev(DataSource dataSource) {
        System.out.println(">>> authEntityManagerFactory - DEV profile active");
        return buildEMF(dataSource, "com.marketpilot.domain.entities.auth", "mp-auth-unit", "update", true);
    }

    @Bean(name = "entityManagerFactory")
    @Profile("test")
    LocalContainerEntityManagerFactoryBean emfTest(DataSource dataSource) {
        return buildEMF(dataSource, "com.marketpilot.domain.entities.auth", "mp-auth-unit", "create-drop", true);
    }

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
