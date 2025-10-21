package com.marketpilot.application.dto.auth;

public enum UserStatus {
    PENDING(1),
    ACTIVE(2),
    SUSPENDED(3),
    CLOSED(4);

    private final int code;
    UserStatus(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
