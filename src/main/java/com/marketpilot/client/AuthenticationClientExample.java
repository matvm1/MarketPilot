package com.marketpilot.client;

import com.marketpilot.adapters.auth.JavaTotpService;
import com.marketpilot.adapters.auth.Password4JHasher;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.services.MfaType;
import com.marketpilot.util.BufferedConverter;
import com.marketpilot.util.Tuple;
import com.marketpilot.adapters.persistence.repo.OjdbcRoleRepository;
import com.marketpilot.adapters.persistence.repo.OjdbcUserRepository;
import com.marketpilot.application.dto.auth.AuthenticationResult;
import com.marketpilot.application.dto.auth.credentials.TotpCredential;
import com.marketpilot.application.ports.auth.SessionManager;
import com.marketpilot.application.services.AuthenticationService;
import com.marketpilot.application.services.AuthenticationService.AuthenticationStatus;
import com.marketpilot.application.services.UserSession;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class AuthenticationClientExample {
    public static void main(String[] args) {
        RoleRepository roleRepository = new OjdbcRoleRepository();
        UserRepository userRepository = new OjdbcUserRepository(roleRepository);
        JavaTotpService javaTotpService = new JavaTotpService(new SystemTimeProvider(), HashingAlgorithm.SHA256);
        PasswordHasher passwordHasher = new Password4JHasher();
        AuthenticationService authenticationService = new AuthenticationService(userRepository, roleRepository, javaTotpService, passwordHasher,
            new SessionManager() {
            @Override
            public Optional<UserSession> createSession(AuthenticationResult authenticationResult) {
                UserSession userSession = new UserSession(1, authenticationResult, Instant.now());
                return Optional.of(userSession);
            }

            @Override
            public Optional<UserSession> getSession(int sessionId) {
                return Optional.empty();
            }

            @Override
            public void invalidate(int sessionId) {

            }
        });
        byte[] passwordLightHash = BufferedConverter.toBytes("987light?Password-Hashcheeto@123");
        //System.out.println(bytesToHex(passwordHasher.hash(passwordLightHash)));
        System.out.println("password entered: " + new String(passwordLightHash, StandardCharsets.UTF_8));
        Tuple<AuthenticationStatus, Optional<UUID>> authResult = authenticationService.initiateEmployeeAuthentication("ADM001",
                passwordLightHash, Role.RoleName.Admin);
        System.out.println("auth status: " +  authResult);

        // Call the service
        String dataUri = javaTotpService.generateSecretQrDataUri("Admin");
        // Display it
        try {
            showQrFromDataUri(dataUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter 6-digit code: ");
        String code = scanner.nextLine();
        if (code.matches("\\d{6}")) {
            System.out.println("Code entered: " + code);
        } else {
            System.out.println("Invalid code. Please enter exactly 6 digits.");
        }
        scanner.close();
        AuthenticationStatus authenticationStatus = authenticationService.completeAuthentication(MfaType.TOTP,
            new TotpCredential(
                    authResult.u().orElse(null),
                    Role.RoleName.Admin,
                    code
            )
        );
        System.out.println(authenticationStatus);
    }

    private static void showQrFromDataUri(String dataUri) throws IOException {
        // Extract base64 part
        String base64 = dataUri.substring(dataUri.indexOf(",") + 1);
        byte[] imageBytes = Base64.getDecoder().decode(base64);

        // Convert to ImageIcon
        ImageIcon icon = new ImageIcon(imageBytes);
        JLabel label = new JLabel(icon);

        // Display in Swing window
        JFrame frame = new JFrame("TOTP QR Code");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        return HexFormat.of().parseHex(hex);
    }
}
