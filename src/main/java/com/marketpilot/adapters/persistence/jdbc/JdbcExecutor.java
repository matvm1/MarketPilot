package com.marketpilot.adapters.persistence.jdbc;

import oracle.jdbc.OracleResultSet;
import oracle.sql.NUMBER;
import oracle.ucp.jdbc.PoolDataSource;

import java.sql.*;
import java.util.*;

@Deprecated
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
                return Optional.ofNullable(resultSetToValue.map(rs));
            }
            return Optional.empty();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data in a List
    public static <T> Optional<Set<T>> fetchToSet(String sql, ResultSetToValue<T> resultSetToSet, Param... params) {
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
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    public static List<Integer> executeUpdateProc(String procName, boolean letProcCommit, Param... params) throws SQLException {
        StringBuilder binders = new StringBuilder("(");
        binders.append("?, ".repeat(Math.max(0, params.length - 1)));
        if (params.length > 0)
            binders.append("?)");
        try (Connection conn = pool.getConnection()) {
            boolean initialAutoCommit = conn.getAutoCommit();
            try (CallableStatement cs = conn.prepareCall("{CALL " + procName + binders + " }")) {
                setParameters(cs, params);

                // either the proc commits or an explicit commit is sent
                conn.setAutoCommit(false);

                boolean hasUpdateCounts = !(cs.execute());
                if (!hasUpdateCounts) {
                    conn.rollback();
                    throw new UnexpectedResultSetException(procName);
                }

                List<Integer> updateCounts = new LinkedList<>();
                while (hasUpdateCounts && cs.getUpdateCount() != -1) {
                    updateCounts.add(cs.getUpdateCount());
                    hasUpdateCounts = cs.getMoreResults();
                }

                if (!letProcCommit)
                    conn.commit();

                return updateCounts;
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
            finally {
                conn.setAutoCommit(initialAutoCommit);
            }
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data cached in an Object[]
    public static <T> Optional<T> fetchToObject(String sql, ResultSetToValue<T> resultSetToValue, Param... params) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);

            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                T cache = resultSetToValue.map(rs);
                return Optional.ofNullable(cache);
            }
            return Optional.empty();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data in a Map
    public static <T, U> Optional<Map<T, U>> fetchToMap(String sql,
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
            e.printStackTrace();
            throw new RuntimeException("Could not execute query:\n" + sql);
        }
    }

    // prepares a PreparedStatement, executes, and returns cached queried data in a multi-map
    public static <ID, T> Optional<Map<ID, Set<T>>> fetchToMultiMap(
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

    // executes an INSERT statement and returns an array with the primary keys ("ID") that were generated
    public static long[] executeInsert(String sql, Param... params) throws SQLException {
        Connection conn = pool.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, new String[] {"ID"} );
        setParameters(ps, params);

        int rowsInserted = ps.executeUpdate();
        ResultSet generatedKeysRs = ps.getGeneratedKeys();

        //ResultSetMetaData meta = generatedKeysRs.getMetaData();
        //int columnCount = meta.getColumnCount();
        // System.out.println("Columns:");
        // for (int i = 1; i <= columnCount; i++) {
        //     System.out.println(i + ": " + meta.getColumnLabel(i));
        //     System.out.println(meta.getColumnType(i));
        // }


        long[] generatedKeys = new long[rowsInserted];
        int i = 0;
        while(generatedKeysRs.next()) {
            // Use oracle.sql.NUMBER rather than java.math.BigDecimal when performance is critical and you are not manipulating the values, just reading and writing them.
            NUMBER number = ((OracleResultSet)generatedKeysRs).getNUMBER(1);
            generatedKeys[i] = number.longValue();
            i++;
        }

        generatedKeysRs.close();
        ps.close();
        conn.close();
        return generatedKeys;
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

    public static class UnexpectedResultSetException extends RuntimeException {
        public UnexpectedResultSetException(String procName) {
            super("Procedure " + procName + " returned a result set, but update counts were expected");
        }
    }

}
