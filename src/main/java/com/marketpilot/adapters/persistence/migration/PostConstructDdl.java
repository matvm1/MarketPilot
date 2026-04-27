package com.marketpilot.adapters.persistence.migration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

// TODO: Manage schema with migrations
public class PostConstructDdl {
    private JdbcClient jdbcClient;

    public PostConstructDdl(JdbcClient jdbcClient, EntityManagerFactory entityManagerFactory) {
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void extendSchema() {
        System.out.println("Extending schema");
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS CLIENT_REGISTRATION_CODE VARCHAR2(16)").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS CLIENT_REGISTRATION_EXPIRATION TIMESTAMP").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS EMPLOYEE_REGISTRATION_CODE VARCHAR2(16)").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS EMPLOYEE_REGISTRATION_EXPIRATION TIMESTAMP").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS CLIENT_TOTP_SECRET VARCHAR2(64 CHAR)").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS EMPLOYEE_TOTP_SECRET VARCHAR2(64 CHAR)").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS CLIENT_USER_STATUS_ID NUMBER(2)").update();
        jdbcClient.sql("ALTER TABLE APP_USER ADD COLUMN IF NOT EXISTS EMPLOYEE_USER_STATUS_ID NUMBER(2)").update();
    }
}
