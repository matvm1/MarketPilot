package com.marketpilot.adapters.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DBConnectionPoolIT {
    @Test
    void constructor_establishesConnection() {
        DBConnectionPool conn = assertDoesNotThrow(DBConnectionPool::new);
        assertTrue(conn.testConnection());
        assertTrue(conn.isConnectionEstablished());
    }
}
