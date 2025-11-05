package com.marketpilot.domain.entities.auth;

public enum UserType {
    CLIENT(1),
    EMPLOYEE(2);

    private final int code;

    UserType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
