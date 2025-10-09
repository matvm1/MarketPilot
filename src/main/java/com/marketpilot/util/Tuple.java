package com.marketpilot.util;

public record Tuple<T, U> (T t, U u) {
    public static <T, U>  Tuple<T, U> of(T t, U u) {
        return new Tuple<>(t, u);
    }
}
