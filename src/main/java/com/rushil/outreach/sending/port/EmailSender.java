package com.rushil.outreach.sending.port;

/**
 * The single seam between this system and whatever actually delivers mail.
 *
 * <p>Everything upstream of this interface — scheduling, templating, suppression —
 * is provider-agnostic. Local development binds a MailHog adapter so the whole
 * system runs from `docker compose up` with no API keys and no real recipients;
 * production binds SES, SendGrid, or Postmark by swapping one bean.
 */
public interface EmailSender {

    SendOutcome send(OutboundEmail email);
}