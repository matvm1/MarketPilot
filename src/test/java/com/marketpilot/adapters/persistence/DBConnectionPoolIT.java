package com.marketpilot.adapters.persistence;

import oracle.ucp.jdbc.PoolDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DBConnectionPoolIT {
    @Test
    void constructor_establishesConnection() {
        assertDoesNotThrow(DBConnectionPool::getPool);
        assertTrue(DBConnectionPool.testConnection());
    }
}
