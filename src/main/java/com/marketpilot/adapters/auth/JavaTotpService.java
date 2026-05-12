package com.marketpilot.adapters.auth;

import com.marketpilot.application.dto.auth.credentials.MfaCredential;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.application.ports.auth.TotpService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.TimeProvider;

import java.util.Arrays;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

// TOTP Verification by https://github.com/samdjstevens/java-totp
public class JavaTotpService implements TotpService {
    private final TimeProvider timeProvider;
    private final HashingAlgorithm hashingAlgorithm;

    public JavaTotpService(TimeProvider timeProvider, HashingAlgorithm hashingAlgorithm) {
        if (timeProvider == null)
            throw new IllegalArgumentException("timeProvider cannot be null");
        if (hashingAlgorithm == null)
            throw new IllegalArgumentException("hashingAlgorithm cannot be null");
        if (hashingAlgorithm != HashingAlgorithm.SHA256 && hashingAlgorithm != HashingAlgorithm.SHA512)
            throw new IllegalArgumentException("hashingAlgorithm must be either SHA256 or SHA512");
        this.timeProvider = timeProvider;
        this.hashingAlgorithm = hashingAlgorithm;
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
        CodeGenerator codeGenerator = new DefaultCodeGenerator(hashingAlgorithm);
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(new String(((TotpCredential) credentials).getSecret()), ((TotpCredential)credentials).getCode());
    }

    @Override
    public String generateSecret() {
        int numCharacters = hashingAlgorithm == HashingAlgorithm.SHA256 ? 52 : 103;
        SecretGenerator secretGenerator = new DefaultSecretGenerator(numCharacters);
        return secretGenerator.generate();
    }

    // returns embeddable data URI for a newly generated secret
    public String generateSecretQrDataUri(String label) {
        // generate secret with padding removed
        String secret = this.generateSecret().replace("=", "");
        QrData.Builder qrDataBuilder = new QrData.Builder()
                .secret(secret)
                .issuer("Market Pilot")
                .algorithm(hashingAlgorithm)
                .digits(6)
                .period(30);

        if (label != null)
            qrDataBuilder.label(label);

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        try {
            byte[] data = qrGenerator.generate(qrDataBuilder.build());
            return getDataUriForImage(data, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            //TODO: log
            e.printStackTrace();
        }

        return "";
    }
}
