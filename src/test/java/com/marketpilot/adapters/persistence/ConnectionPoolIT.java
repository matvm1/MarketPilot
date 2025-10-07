package com.marketpilot.adapters.persistence;

import com.marketpilot.adapters.persistence.jdbc.ConnectionPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectionPoolIT {
    @Test
    void constructor_establishesConnection() {
        assertDoesNotThrow(ConnectionPool::getPool);
        assertTrue(ConnectionPool.testConnection());
    }
}
