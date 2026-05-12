package com.marketpilot.adapters.persistence.migration;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.simple.JdbcClient;

// TODO: Manage schema with migrations
public class PostConstructDdl {
    private final JdbcClient jdbcClient;

    public PostConstructDdl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void extendSchema() {
        System.out.println("Extending schema");

        jdbcClient.sql("""
                CREATE TABLE APP_USER_AUTH (
                    USER_ID                          NUMBER PRIMARY KEY,
                    UUID                             RAW(16) NOT NULL,
                    MFATYPE_ID                       NUMBER(2),
                    IS_CLIENT                        BOOLEAN DEFAULT FALSE NOT NULL,
                    CLIENT_PASSWORD_HASH             RAW(200) NULL,
                    CLIENT_REGISTRATION_CODE         VARCHAR2(16),
                    CLIENT_REGISTRATION_EXPIRATION   TIMESTAMP DEFAULT (SYSTIMESTAMP + INTERVAL '30' MINUTE) NULL,
                    CLIENT_TOTP_SECRET               RAW(200) NULL,
                    CLIENT_USER_STATUS_ID            NUMBER(2),
                    IS_EMPLOYEE                      BOOLEAN DEFAULT FALSE NOT NULL,
                    EMPLOYEE_PASSWORD_HASH           RAW(200) NULL,
                    EMPLOYEE_REGISTRATION_CODE       VARCHAR2(16),
                    EMPLOYEE_REGISTRATION_EXPIRATION TIMESTAMP DEFAULT (SYSTIMESTAMP + INTERVAL '30' MINUTE) NULL,
                    EMPLOYEE_TOTP_SECRET             RAW(200) NULL,
                    EMPLOYEE_USER_STATUS_ID          NUMBER(2),
                
                    CONSTRAINT FK_APP_USER_AUTH FOREIGN KEY (USER_ID) REFERENCES APP_USER(ID)
                );""").update();
    }
}