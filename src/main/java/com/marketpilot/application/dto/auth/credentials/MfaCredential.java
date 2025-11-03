package com.marketpilot.application.dto.auth.credentials;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.UUID;

public abstract class MfaCredential {
    protected final Role.RoleName roleName;
    private final UUID userUuid;
    private final UserType userType;

    public MfaCredential(UUID userUuid, UserType userType, Role.RoleName roleName) {
        if (userUuid == null)
            throw new IllegalArgumentException("userUuid cannot be null");
        if (userType == null)
            throw new IllegalArgumentException("userType cannot be null");
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");

        this.userUuid = userUuid;
        this.userType = userType;
        this.roleName = roleName;
    }

    public UUID getUserUuid() { return userUuid; }
    public UserType getUserType() { return userType; }

    public Role.RoleName getRoleName() { return roleName; }
}
