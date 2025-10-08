package com.marketpilot.adapters.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetToValue<T> {
    T map(ResultSet rs) throws SQLException;
}