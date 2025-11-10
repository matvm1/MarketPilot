package com.marketpilot.adapters.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaTotpServiceTest {

    private JavaTotpService javaTotpService;
    @Mock private TimeProvider mockTimeProvider;
    private final char[] dummySecret = "CH4LOY3ZDPAI3ZXPRLZSWUQGF6ZYSJIPLTS6GGU7K4WJGK2O5VVA====".toCharArray();
    private final HashingAlgorithm dummyHashingAlorithm = HashingAlgorithm.SHA256;

    @BeforeEach
    void setUp() {
        javaTotpService = new JavaTotpService(mockTimeProvider, dummyHashingAlorithm);
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
        long fixedTime = 1_700_000_000_000L;
        when(mockTimeProvider.getTime()).thenReturn(fixedTime);
        TotpCredential credential = new TotpCredential("012345");
        credential.setSecret(dummySecret);

        assertFalse(javaTotpService.verify(credential));
    }

    //TODO: Test with 64-byte secret
    @Test
    void verify_returnsTrue_forValidCodeInFixedTime() {
        long fixedTime = 1_700_000_000_000L;
        when(mockTimeProvider.getTime()).thenReturn(fixedTime);
        TotpCredential credential = new TotpCredential("416039");
        credential.setSecret(dummySecret);

        assertTrue(javaTotpService.verify(credential));
    }

    @Test
    void verify_returnsTrue_forValidCodeInCurrentTime() {
        long currentTime = System.currentTimeMillis();
        int interval = 30;
        when(mockTimeProvider.getTime()).thenReturn(currentTime);

        CodeGenerator codeGenerator = new DefaultCodeGenerator(dummyHashingAlorithm);
        String validCode;
        try {
            validCode = codeGenerator.generate(Arrays.toString(dummySecret), currentTime / interval);
        } catch (CodeGenerationException e) {
            throw new RuntimeException(e);
        }

        TotpCredential credential = new TotpCredential(validCode);
        credential.setSecret(dummySecret);

        assertTrue(javaTotpService.verify(credential));
    }
}
