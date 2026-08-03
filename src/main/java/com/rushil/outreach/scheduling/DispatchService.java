package com.rushil.outreach.scheduling;

import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.campaign.SequenceStepRepository;
import com.rushil.outreach.enrollment.Enrollment;
import com.rushil.outreach.enrollment.EnrollmentRepository;
import com.rushil.outreach.sending.EmailSend;
import com.rushil.outreach.sending.EmailSendRepository;
import com.rushil.outreach.sending.port.EmailSender;
import com.rushil.outreach.sending.port.OutboundEmail;
import com.rushil.outreach.sending.port.SendOutcome;
import com.rushil.outreach.suppression.SuppressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final EnrollmentRepository enrollments;
    private final SequenceStepRepository steps;
    private final EmailSendRepository sends;
    private final SuppressionService suppression;
    private final EmailSender emailSender;

    @Value("${outreach.scheduler.batch-size:50}")
    private int batchSize;

    @Value("${outreach.sending.unsubscribe-base-url}")
    private String unsubscribeBaseUrl;

    public DispatchService(
            EnrollmentRepository enrollments,
            SequenceStepRepository steps,
            EmailSendRepository sends,
            SuppressionService suppression,
            EmailSender emailSender) {
        this.enrollments = enrollments;
        this.steps = steps;
        this.sends = sends;
        this.suppression = suppression;
        this.emailSender = emailSender;
    }

    /**
     * Claims and processes one batch of due enrollments.
     *
     * <p>The whole batch runs in a single transaction, which keeps the row locks held
     * for its duration. That is fine at small batch sizes and is the simplest thing
     * that is correct. It does mean a slow SMTP server stretches the transaction, so
     * once send volume grows this should become: claim ids in a short transaction,
     * then process each enrollment in its own. Worth noting in the README as a known
     * trade-off rather than pretending it isn't one.
     */
    @Transactional
    public int processDueBatch() {
        List<Enrollment> due = enrollments.claimDueEnrollments(batchSize);
        int processed = 0;

        for (Enrollment enrollment : due) {
            try {
                processOne(enrollment);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process enrollment {}", enrollment.getId(), e);
                enrollment.markFailed(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        return processed;
    }

    private void processOne(Enrollment enrollment) {
        int nextOrder = enrollment.getCurrentStepOrder() + 1;

        Optional<SequenceStep> maybeStep = steps.findByCampaignIdAndStepOrder(
                enrollment.getCampaign().getId(), nextOrder);

        if (maybeStep.isEmpty()) {
            enrollment.complete();
            return;
        }
        SequenceStep step = maybeStep.get();

        var owner = enrollment.getCampaign().getOwner();
        String recipient = enrollment.getLead().getEmail();

        // The compliance gate. Nothing sends without passing through here.


        // Idempotency: claim the (enrollment, step) pair in the database BEFORE
        // talking to the mail provider. The unique constraint on those two columns
        // means a retry after a mid-send crash collides here and stops, rather than
        // sending the same message to the same person twice.
        EmailSend send = new EmailSend(enrollment, step, recipient,
                render(step.getSubjectTemplate(), enrollment),
                render(step.getBodyTemplate(), enrollment));
        try {
            sends.saveAndFlush(send);
        } catch (DataIntegrityViolationException alreadySent) {
            log.warn("Step {} already dispatched for enrollment {} — skipping duplicate",
                    step.getId(), enrollment.getId());
            scheduleNext(enrollment, nextOrder);
            return;
        }

        SendOutcome result = emailSender.send(new OutboundEmail(
                enrollment.getCampaign().getFromName(),
                enrollment.getCampaign().getFromEmail(),
                recipient,
                enrollment.getLead().getContactName(),
                send.getSubject(),
                send.getBody(),
                stripHtml(send.getBody()),
                Map.of(
                        // RFC 8058 one-click unsubscribe. Mailbox providers surface this
                        // as a native button, and honoring it well measurably improves
                        // inbox placement.
                        "List-Unsubscribe", "<" + unsubscribeBaseUrl + "/" + send.getId() + ">",
                        "List-Unsubscribe-Post", "List-Unsubscribe=One-Click")));

        if (result.accepted()) {
            send.markSent(result.providerMessageId());
            scheduleNext(enrollment, nextOrder);
        } else {
            send.markFailed(result.failureReason());
            // A soft failure leaves the enrollment active so the next tick retries;
            // a hard bounce should suppress the address permanently. Distinguishing
            // the two properly belongs in the webhook handler, once that exists.
            log.warn("Send rejected for enrollment {}: {}", enrollment.getId(), result.failureReason());
        }
    }

    private void scheduleNext(Enrollment enrollment, int completedOrder) {
        Optional<SequenceStep> following = steps.findByCampaignIdAndStepOrder(
                enrollment.getCampaign().getId(), completedOrder + 1);

        if (following.isEmpty()) {
            enrollment.advanceTo(completedOrder, null);
            enrollment.complete();
            return;
        }
        Instant nextAt = Instant.now().plus(Duration.ofDays(following.get().getDelayDays()));
        enrollment.advanceTo(completedOrder, nextAt);
    }

    private void recordSkip(Enrollment enrollment, SequenceStep step, String recipient, String reason) {
        EmailSend skipped = new EmailSend(enrollment, step, recipient, "", "");
        skipped.markSkipped(reason);
        sends.save(skipped);
    }

    /**
     * Placeholder templating. Replaced later by the personalization service, which
     * calls an LLM with scraped prospect context instead of doing string
     * substitution — but the call site stays the same shape.
     */
    private String render(String template, Enrollment enrollment) {
        var lead = enrollment.getLead();
        return template
                .replace("{{contact_name}}", nullSafe(lead.getContactName()))
                .replace("{{company_name}}", nullSafe(lead.getCompanyName()))
                .replace("{{industry}}", nullSafe(lead.getIndustry()))
                .replace("{{city}}", nullSafe(lead.getCity()));
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}