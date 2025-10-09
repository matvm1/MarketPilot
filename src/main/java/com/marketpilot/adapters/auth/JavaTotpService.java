package com.marketpilot.adapters.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.application.ports.auth.TwoFactorService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.TimeProvider;

import java.util.Arrays;

// TOTP Verification by https://github.com/samdjstevens/java-totp
public class JavaTotpService implements TwoFactorService {
    private final TimeProvider timeProvider;

    public JavaTotpService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    //TODO: Unit tests
    @Override
    public boolean verify(MfaCredential credentials) {
        if (credentials == null)
            return false;

        if (!(credentials instanceof TotpCredential))
            return false;

        //TODO: hash with SHA-512
        //TODO: Support 64 byte (103 character) secret generation / QR code
        // May have DB schema implications
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        boolean res = verifier.isValidCode(Arrays.toString(((TotpCredential) credentials).getSecret()), ((TotpCredential)credentials).getCode());
        System.out.println(res);
        return res;
    }
}
