package com.marketpilot.adapters.persistence.util;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlExceptionBiFunction<T, U, R> {
    R apply(T t, U u) throws SQLException;
}
