package com.marketpilot.application.dto.auth.credentials;

import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.UserType;

import java.util.Arrays;
import java.util.UUID;

public class TotpCredential extends MfaCredential {
    private final RoleName roleName;
    private char[] secret;
    private final String code;

    public TotpCredential(UUID userUuid, UserType userType, RoleName roleName, String code) {
        super(userUuid, userType);
        if (roleName == null)
            throw new IllegalArgumentException("roleName cannot be null");
        if (code == null)
            throw new IllegalArgumentException("code cannot be null");
        if (code.isBlank())
            throw new IllegalArgumentException("code cannot be blank");

        this.roleName = roleName;
        this.code = code;
    }

    public RoleName getRoleName() { return roleName; }
    public char[] getSecret() { return secret; }
    public String getCode() { return code; }

    public final void setSecret(char[] secret) {
        if (secret == null)
            throw new IllegalArgumentException("secret cannot be null");
        if (Arrays.toString(secret).isBlank())
            throw new IllegalArgumentException("secret cannot be blank");

        this.secret = secret;
    }
}
