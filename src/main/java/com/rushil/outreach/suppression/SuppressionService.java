package com.rushil.outreach.suppression;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * The compliance gate. Every send path must call {@link #isSuppressed} before
 * dispatch, with no bypass.
 *
 * <p>US CAN-SPAM requires a working opt-out that is honored promptly, a real
 * physical mailing address in the message, and honest headers and subject lines.
 * The suppression list is how the first of those is actually enforced at runtime
 * rather than merely promised in a footer. Hard bounces are added here too: repeated
 * sends to dead addresses are what wreck a sending domain's reputation.
 *
 * <p>Suppression is scoped per owner, not global, because two customers of the same
 * platform legitimately may have different relationships with the same address.
 * Note that this is the US baseline — GDPR requires a lawful basis for contacting
 * EU recipients and is considerably stricter, so geo-gating is worth adding before
 * this ever touches European contacts.
 */
@Service
public class SuppressionService {

    private final SuppressionRepository repository;

    public SuppressionService(SuppressionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void suppress(UUID ownerId, String email, SuppressionReason reason) {
        String normalized = normalize(email);
        repository.save(new SuppressionEntry(ownerId, normalized, reason));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}