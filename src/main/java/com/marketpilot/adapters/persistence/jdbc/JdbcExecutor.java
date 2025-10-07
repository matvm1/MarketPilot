package com.marketpilot.adapters.persistence.jdbc;

import oracle.ucp.jdbc.PoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Do not expose outside MarketPilot project
//TODO: validate incoming sql
public class JdbcExecutor {
    private static final PoolDataSource pool = ConnectionPool.getPool();

    private JdbcExecutor() {}

    // prepares a PreparedStatement, executes, and returns the queried data wrapped in a ResultSet
    public static ResultSet executeQuery(String sql, Param... params) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeQuery();
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns count of affected records
    public static int executeUpdate(String sql, Param... params) {
        try (Connection conn = pool.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    public static int[] executeUpdateBatch(String sql, Batch[] batches) {
        try (Connection conn = pool.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Batch batch : batches) {
                setParameters(ps, batch.params());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // sets parameters on a prepared statement
    private static void setParameters(PreparedStatement ps, Param... params) throws SQLException {
        for (Param p : params) {
            ps.setObject(p.index(), p.value(), p.sqlType());
        }
    }
}
