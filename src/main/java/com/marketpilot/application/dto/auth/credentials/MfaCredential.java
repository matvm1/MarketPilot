package com.marketpilot.application.dto.auth.credentials;

import com.marketpilot.domain.entities.auth.UserType;

import java.util.UUID;

public abstract class MfaCredential {
    private final UUID userUuid;
    private final UserType userType;

    public MfaCredential(UUID userUuid, UserType userType) {
        if (userUuid == null)
            throw new IllegalArgumentException("userUuid cannot be null");
        if (userType == null)
            throw new IllegalArgumentException("userType cannot be null");

        this.userUuid = userUuid;
        this.userType = userType;
    }

    public UUID getUserUuid() { return userUuid; }
    public UserType getUserType() { return userType; }
}
