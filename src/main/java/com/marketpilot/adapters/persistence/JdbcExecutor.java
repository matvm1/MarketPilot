package com.marketpilot.adapters.persistence;

import oracle.ucp.jdbc.PoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

// Do not expose outside MarketPilot project
public class JdbcExecutor {
    private static final PoolDataSource pool = ConnectionPool.getPool();

    private JdbcExecutor() {}

    // prepares a PreparedStatement, executes, and returns the queried data wrapped in a ResultSet
    public static ResultSet executeQuery(String sql, Param... params) {
        try (PreparedStatement ps = prepareStatement(sql, params)) {
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
        try (PreparedStatement ps = prepareStatement(sql, params)) {
            return ps.executeUpdate();
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement with the query in String sql and varargs params
    private static PreparedStatement prepareStatement(String sql, Param... params) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Param p : params)
                ps.setObject(p.index(), p.value(), p.sqlType());
            return ps;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not prepare statement:\nsql: " + sql + "\nparams:" + Arrays.toString(params));
        }
    }
}
