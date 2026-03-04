package com.marketpilot.util;

public class EqualityUtil {
    public static boolean noneNull(Object... o) {
        if (o == null || o.length == 0)
            return false;

        for (Object object : o)
            if (object == null)
                return false;

        return true;
    }

    public static boolean allNull(Object... o) {
        if (o == null || o.length == 0)
            return false;

        for (Object object : o)
            if (object != null)
                return false;

        return true;
    }
}
