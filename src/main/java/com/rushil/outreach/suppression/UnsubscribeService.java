package com.rushil.outreach.suppression;

import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.enrollment.Enrollment;
import com.rushil.outreach.enrollment.EnrollmentRepository;
import com.rushil.outreach.enrollment.EnrollmentStatus;
import com.rushil.outreach.sending.EmailSend;
import com.rushil.outreach.sending.EmailSendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles opt-outs arriving from the link in a sent email.
 *
 * <p>The token is the {@code email_send} id: unguessable, scoped to one message,
 * and it identifies the recipient without putting their address in a URL that ends
 * up in server logs and referrer headers.
 *
 * <p>Opting out suppresses the address for the whole owner account, not just this
 * campaign. Someone who says stop means stop — quietly continuing to mail them from
 * a different campaign of the same sender is exactly what CAN-SPAM's opt-out
 * requirement exists to prevent.
 */
@Service
public class UnsubscribeService {

    private static final Logger log = LoggerFactory.getLogger(UnsubscribeService.class);

    private final EmailSendRepository sends;
    private final EnrollmentRepository enrollments;
    private final SuppressionService suppression;

    public UnsubscribeService(EmailSendRepository sends, EnrollmentRepository enrollments,
                              SuppressionService suppression) {
        this.sends = sends;
        this.enrollments = enrollments;
        this.suppression = suppression;
    }

    @Transactional
    public void unsubscribeByToken(UUID sendId) {
        EmailSend send = sends.findById(sendId)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown unsubscribe token"));

        Enrollment source = send.getEnrollment();
        UUID ownerId = source.getCampaign().getOwner().getId();
        String email = send.getToEmail();

        suppression.suppress(ownerId, email, SuppressionReason.UNSUBSCRIBED);

        // Stop every active sequence this lead is in for this owner, not just the
        // one they happened to click from.
        List<Enrollment> active = enrollments.findAllByLeadIdAndStatus(
                source.getLead().getId(), EnrollmentStatus.ACTIVE);

        for (Enrollment enrollment : active) {
            enrollment.markUnsubscribed();
        }
        log.info("Unsubscribed lead {} and stopped {} active enrollment(s)",
                source.getLead().getId(), active.size());
    }
}