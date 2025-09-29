package com.marketpilot.application.dto.auth.credentials;

import com.marketpilot.domain.entities.auth.Role.RoleName;

import java.util.UUID;

public class TotpCredential extends MfaCredential {
    private final UUID userUuid;
    private final RoleName roleName;
    private final String secret;
    private final String code;

    public TotpCredential(UUID userUuid, RoleName roleName, String secret, String code) {
        if (userUuid == null)
            throw new IllegalArgumentException("userUuid cannot be null");
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");
        if (secret == null)
            throw new IllegalArgumentException("secret cannot be null");
        if (secret.isBlank())
            throw new IllegalArgumentException("secret cannot be blank");
        if (code == null)
            throw new IllegalArgumentException("code cannot be null");
        if (code.isBlank())
            throw new IllegalArgumentException("code cannot be blank");

        this.userUuid = userUuid;
        this.roleName = roleName;
        this.secret = secret;
        this.code = code;
    }

    public UUID getUserUuid() { return userUuid; }
    public RoleName getRoleName() { return roleName; }
    public String getSecret() { return secret; }
    public String getCode() { return code; }
}
