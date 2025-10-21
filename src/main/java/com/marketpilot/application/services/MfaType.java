package com.marketpilot.application.services;


public enum MfaType {
    TOTP(1);

    private final int code;
    MfaType(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
    public static MfaType fromCode(int code) {
        for (MfaType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MFA type code: " + code);
    }
}