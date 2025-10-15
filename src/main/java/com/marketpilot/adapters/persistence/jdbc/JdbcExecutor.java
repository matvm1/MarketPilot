package com.marketpilot.adapters.persistence.jdbc;

import oracle.ucp.jdbc.PoolDataSource;

import java.sql.*;
import java.util.*;

// Do not expose outside MarketPilot project
//TODO: validate incoming sql
//TODO: unit tests, integration tests
public class JdbcExecutor {
    private static final PoolDataSource pool = ConnectionPool.getPool();

    private JdbcExecutor() {}

    public static <T> Optional<T> fetchRecord(String sql, ResultSetToValue<T> resultSetToValue, Param... params) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                return Optional.of(resultSetToValue.map(rs));
            }
            return Optional.empty();
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data in a List
    public static <T> Optional<Set<T>> executeQueryToSet(String sql, ResultSetToValue<T> resultSetToSet, Param... params) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);

            ResultSet rs = ps.executeQuery();

            Set<T> dataCache = new HashSet<>();
            while (rs.next())
                dataCache.add(resultSetToSet.map(rs));
            return dataCache.isEmpty() ? Optional.empty() : Optional.of(dataCache);
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data cached in a Tuple
    /*public static <T, U> Optional<Tuple<T, U>> executeQueryToTuple(String sql,
           ResultSetToValue<T> resultSetToValueT,
           ResultSetToValue<U> resultSetToValueU,
           Param... params) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);

            ResultSet rs = ps.executeQuery();

            Tuple<T, U> dataCache = new Tuple<T, U>(resultSetToValueT.map(rs), resultSetToValueU.map(rs));
            return Optional.of(dataCache);
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }*/

    // prepares a PreparedStatement, executes, and returns cached queried data in a Map
    public static <T, U> Optional<Map<T, U>> executeQueryToMap(String sql,
       ResultSetToValue<T> resultSetToValueT,
       ResultSetToValue<U> resultSetToValueU,
       Param... params) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);

            ResultSet rs = ps.executeQuery();

            Map<T, U> dataCache = new HashMap<>();
            while (rs.next()) {
                dataCache.put(resultSetToValueT.map(rs), resultSetToValueU.map(rs));
            }
            return dataCache.isEmpty() ? Optional.empty() : Optional.of(dataCache);
        }
        catch (SQLException e) {
            //TODO: Rollback? Commit?
            //TODO: Log
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data in a multi-map
    public static <ID, T> Optional<Map<ID, Set<T>>> executeQueryToMultiMap(
            String sql,
            ResultSetToValue<ID> keyMapper,
            ResultSetToValue<T> valueMapper,
            Param... params
    ) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);

            ResultSet rs = ps.executeQuery();

            Map<ID, Set<T>> dataCache = new HashMap<>();
            while (rs.next()) {
                ID key = keyMapper.map(rs);
                T value = valueMapper.map(rs);

                dataCache.computeIfAbsent(key, k -> new HashSet<>()).add(value);
            }
            return dataCache.isEmpty() ? Optional.empty() : Optional.of(dataCache);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns count of affected records
    public static int executeUpdate(String sql, Param... params) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        setParameters(ps, params);
        int rowsAffected = ps.executeUpdate();
        ps.close();
        conn.close();
        return rowsAffected;
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
