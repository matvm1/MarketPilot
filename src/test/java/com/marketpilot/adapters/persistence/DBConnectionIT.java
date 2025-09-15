package com.marketpilot.adapters.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DBConnectionIT {
    @Test
    void constructor_establishesConnection() {
        DBConnection conn = assertDoesNotThrow(DBConnection::new);
        assertTrue(conn.testConnection());
        assertTrue(conn.isConnectionEstablished());
    }
}
