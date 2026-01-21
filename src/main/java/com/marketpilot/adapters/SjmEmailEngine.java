package com.marketpilot.adapters;

import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.EmailEngine;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Simple Java Mail
public class SjmEmailEngine implements EmailEngine {
    private static Mailer mailer;
    private static String smtpHost;
    private static String smtpEmail;
    private static String smtpPassword;

    public SjmEmailEngine() {
        readCredentials();

        mailer = MailerBuilder
                .withSMTPServer(smtpHost, 587, smtpEmail, smtpPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
    }

    @Override
    public boolean sendEmail(EmailMessage message) {
        Email email = EmailBuilder.startingBlank()
                .from(smtpEmail)
                .to(message.recipient())
                .withSubject(message.subject())
                .withPlainText(message.body())
                .buildEmail();

        mailer.sendMail(email);
        return true;
    }

    @Override
    public boolean sendTemplatedEmail(EmailMessage message, String templateFileName) {
        String htmlBody;
        try {
            htmlBody = PebbleHtmlTemplateEngine.getInstance().render(templateFileName, message.vars());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Email email = EmailBuilder.startingBlank()
                .from(smtpEmail)
                .to(message.recipient())
                .withSubject(message.subject())
                .withHTMLText(htmlBody)
                .buildEmail();

        mailer.sendMail(email);
        return true;
    }

    private static void readCredentials() {
        String propsPath = System.getenv("SMTP_PROPERTIES_PATH");

        if (propsPath == null) {
            throw new IllegalArgumentException("SMTP_PROPERTIES_PATH environment variable is not set");
        }

        Properties props = new Properties();
        try(FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        smtpHost = props.getProperty("SMTP_HOST");
        smtpEmail = props.getProperty("SMTP_EMAIL");
        smtpPassword = props.getProperty("SMTP_PASSWORD");
    }
}
