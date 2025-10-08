package com.marketpilot.adapters.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@FunctionalInterface
public interface ResultSetToMap<K, T> {
    Map<K, T> map(ResultSet rs) throws SQLException;
}
