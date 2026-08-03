package com.rushil.outreach.sending;

import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.common.CreatedEntity;
import com.rushil.outreach.enrollment.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One attempt to deliver one step to one lead.
 *
 * <p>The unique constraint on (enrollment_id, step_id) in the schema is the
 * idempotency key for the whole system. The dispatcher inserts this row *before*
 * calling the mail provider, so a crash between insert and send leaves a QUEUED row
 * that a retry collides with rather than a silent duplicate delivery. That ordering
 * is deliberate and easy to break by "optimising" the insert to happen after the
 * send — don't.
 */
@Entity
@Table(name = "email_send")
public class EmailSend extends CreatedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id", nullable = false, updatable = false)
    private SequenceStep step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SendStatus status = SendStatus.QUEUED;

    @Column(name = "to_email", nullable = false, length = 320)
    private String toEmail;

    @Column(nullable = false, columnDefinition = "text")
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected EmailSend() {
    }

    public EmailSend(Enrollment enrollment, SequenceStep step, String toEmail,
                     String subject, String body) {
        this.enrollment = enrollment;
        this.step = step;
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
    }

    public void markSent(String providerMessageId) {
        this.status = SendStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = Instant.now();
        this.attemptCount++;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = SendStatus.FAILED;
        this.failureReason = reason;
        this.attemptCount++;
    }

    public void markSkipped(String reason) {
        this.status = SendStatus.SKIPPED;
        this.failureReason = reason;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public SequenceStep getStep() {
        return step;
    }

    public SendStatus getStatus() {
        return status;
    }

    public String getToEmail() {
        return toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}