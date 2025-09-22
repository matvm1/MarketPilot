package com.marketpilot.application.dto;

import java.util.Map;

public record EmailMessage(
        String recipient,
        String subject,
        String htmlBody,
        Map<String, Object> vars
) {}
