package com.marketpilot.adapters.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.application.ports.auth.TwoFactorService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

// TOTP Verification by https://github.com/samdjstevens/java-totp
public class JavaTotpService implements TwoFactorService {
    //TODO: Unit tests
    @Override
    public boolean verify(MfaCredential credentials) {
        if (credentials == null)
            return false;

        if (!(credentials instanceof TotpCredential))
            return false;

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA512);
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        return verifier.isValidCode(((TotpCredential)credentials).getSecret(), ((TotpCredential)credentials).getCode());
    }
}
