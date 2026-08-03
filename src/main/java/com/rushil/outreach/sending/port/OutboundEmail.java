package com.rushil.outreach.sending.port;

import java.util.Map;

public record OutboundEmail(
        String fromName,
        String fromEmail,
        String toEmail,
        String toName,
        String subject,
        String htmlBody,
        String textBody,
        Map<String, String> headers) {

    public OutboundEmail {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}