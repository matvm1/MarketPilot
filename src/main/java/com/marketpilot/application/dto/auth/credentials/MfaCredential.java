package com.marketpilot.application.dto.auth.credentials;

import java.util.UUID;

public abstract class MfaCredential {
    protected final UUID userUuid;

    public MfaCredential(UUID userUuid) {
        if (userUuid == null)
            throw new IllegalArgumentException("userUuid cannot be null");
        this.userUuid = userUuid;
    }

    public UUID getUserUuid() { return userUuid; }
}
