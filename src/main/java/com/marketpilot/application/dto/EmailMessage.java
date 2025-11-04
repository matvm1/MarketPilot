package com.marketpilot.application.dto;

import java.util.Map;

public record EmailMessage(
        String recipient,
        String subject,
        String body,
        Map<String, Object> vars
) {}
