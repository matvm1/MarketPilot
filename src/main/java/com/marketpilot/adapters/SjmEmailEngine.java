package com.marketpilot.adapters;

import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.HtmlTemplateEngine;
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
    private HtmlTemplateEngine htmlTemplateEngine;
    private Mailer mailer;
    private String smtpHost;
    private String smtpEmail;
    private String smtpPassword;

    public SjmEmailEngine(HtmlTemplateEngine htmlTemplateEngine, String smtpHost, String smtpEmail, String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpEmail = smtpEmail;
        this.smtpPassword = smtpPassword;

        mailer = MailerBuilder
                .withSMTPServer(smtpHost, 587, smtpEmail, smtpPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();

        this.htmlTemplateEngine = htmlTemplateEngine;
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
            htmlBody = htmlTemplateEngine.render(templateFileName, message.vars());
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
}
