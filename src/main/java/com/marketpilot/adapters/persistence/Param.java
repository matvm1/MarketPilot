package com.marketpilot.adapters.persistence;

import java.sql.*;
import java.math.BigDecimal;

public record Param(int index, Object value, int sqlType) {
    public static Param intP(int i, int v)       { return new Param(i, v, Types.INTEGER); }
    public static Param longP(int i, long v)     { return new Param(i, v, Types.BIGINT); }
    public static Param stringP(int i, String v)    { return new Param(i, v, Types.VARCHAR); }
    public static Param bigdecimalToNumericP(int i, BigDecimal v) { return new Param(i, v, Types.NUMERIC); }
    public static Param bigdecimalToDecimalP(int i, BigDecimal v) { return new Param(i, v, Types.DECIMAL); }
    public static Param booleanP(int i, boolean v)  { return new Param(i, v, Types.BOOLEAN); }
    public static Param decimalP(int i, BigDecimal v){ return new Param(i, v, Types.DECIMAL); }
    public static Param dateP(int i, Date v)     { return new Param(i, v, Types.DATE); }
    public static Param timestampP(int i, Timestamp v)  { return new Param(i, v, Types.TIMESTAMP); }
    public static Param timeP(int i, Time v)     { return new Param(i, v, Types.TIME); }
    public static Param nullP(int i, int type)   { return new Param(i, null, Types.NULL); }
}
