package com.marketpilot.adapters.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaTotpServiceTest {

    private JavaTotpService javaTotpService;
    @Mock private TimeProvider mockTimeProvider;

    @BeforeEach
    void setUp() {
        javaTotpService = new JavaTotpService(mockTimeProvider, HashingAlgorithm.SHA256);
    }

    @Test
    void verify_returnsFalse_forNullCredential() {
        assertFalse(javaTotpService.verify(null));
    }

    @Test
    void verify_returnsFalse_forInvalidCredentialType() {
        MfaCredential mockCredential = mock(MfaCredential.class);
        assertFalse(javaTotpService.verify(mockCredential));
    }

    @Test
    void verify_returnsFalse_forInvalidCode() {
        String secret = "JBSWY3DPEHPK3PXP";
        long fixedTime = 1_700_000_000_000L;
        when(mockTimeProvider.getTime()).thenReturn(fixedTime);
        TotpCredential credential = new TotpCredential(UUID.randomUUID(), RoleName.PersonalInvestor,  "012345");

        assertFalse(javaTotpService.verify(credential));
    }

    //TODO: Test with 64-byte secret
    @Test
    void verify_returnsTrue_forValidCodeInFixedTime() {
        String secret = "JBSWY3DPEHPK3PXP";
        long fixedTime = 1_700_000_000_000L;
        when(mockTimeProvider.getTime()).thenReturn(fixedTime);
        TotpCredential credential = new TotpCredential(UUID.randomUUID(), RoleName.PersonalInvestor, "813407");

        assertTrue(javaTotpService.verify(credential));
    }

    @Test
    void verify_returnsTrue_forValidCodeInCurrentTime() {
        String secret = "JBSWY3DPEHPK3PXP";
        long currentTime = System.currentTimeMillis();
        int interval = 30;
        when(mockTimeProvider.getTime()).thenReturn(currentTime);

        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA512);
        String validCode = null;
        try {
            validCode = codeGenerator.generate(secret, currentTime / interval);
        } catch (CodeGenerationException e) {
            throw new RuntimeException(e);
        }

        TotpCredential credential = new TotpCredential(UUID.randomUUID(), RoleName.PersonalInvestor, validCode);

        assertTrue(javaTotpService.verify(credential));
    }
}
