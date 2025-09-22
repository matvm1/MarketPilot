package com.marketpilot.application.ports;

import com.marketpilot.application.dto.EmailMessage;

public interface EmailEngine {
    boolean sendEmail(EmailMessage message);
    boolean sendTemplatedEmail(EmailMessage message, String templateFileName);
}