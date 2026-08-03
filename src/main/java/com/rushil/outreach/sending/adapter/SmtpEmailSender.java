package com.rushil.outreach.sending.adapter;

import com.rushil.outreach.sending.port.EmailSender;
import com.rushil.outreach.sending.port.OutboundEmail;
import com.rushil.outreach.sending.port.SendOutcome;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

/**
 * Plain SMTP adapter. Points at MailHog locally (localhost:1025, web UI on :8025),
 * where every message is captured and nothing leaves the machine.
 */
@Component
@ConditionalOnProperty(name = "outreach.sending.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public SendOutcome send(OutboundEmail email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email.fromEmail(), email.fromName());
            helper.setTo(email.toEmail());
            helper.setSubject(email.subject());
            helper.setText(email.textBody(), email.htmlBody());

            for (Map.Entry<String, String> header : email.headers().entrySet()) {
                message.setHeader(header.getKey(), header.getValue());
            }

            mailSender.send(message);

            String messageId = message.getMessageID();
            return SendOutcome.accepted(messageId != null ? messageId : UUID.randomUUID().toString());

        } catch (UnsupportedEncodingException e) {
            return SendOutcome.rejected("Invalid from-name encoding: " + e.getMessage());
        } catch (Exception e) {
            log.warn("SMTP send failed for {}: {}", email.toEmail(), e.getMessage());
            return SendOutcome.rejected(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}