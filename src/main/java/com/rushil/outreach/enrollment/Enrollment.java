package com.rushil.outreach.enrollment;

import com.rushil.outreach.campaign.Campaign;
import com.rushil.outreach.common.BaseEntity;
import com.rushil.outreach.lead.Lead;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "enrollment")
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false, updatable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", nullable = false, updatable = false)
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(name = "current_step_order", nullable = false)
    private int currentStepOrder = 0;

    @Column(name = "next_action_at")
    private Instant nextActionAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "end_reason", length = 255)
    private String endReason;

    @Version
    @Column(nullable = false)
    private long version;

    protected Enrollment() {
    }

    public Enrollment(Campaign campaign, Lead lead) {
        this.campaign = campaign;
        this.lead = lead;
    }

    public void start(Instant firstActionAt) {
        requireNotTerminal();
        if (status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("Enrollment already started: " + getId());
        }
        this.status = EnrollmentStatus.ACTIVE;
        this.startedAt = Instant.now();
        this.nextActionAt = firstActionAt;
    }

    public void advanceTo(int stepOrder, Instant nextActionAt) {
        requireActive();
        this.currentStepOrder = stepOrder;
        this.nextActionAt = nextActionAt;
    }

    public void complete() {
        requireActive();
        stop(EnrollmentStatus.COMPLETED, "sequence finished");
    }

    public void markReplied() {
        stopIfRunning(EnrollmentStatus.REPLIED, "lead replied");
    }

    public void markBounced(String detail) {
        stopIfRunning(EnrollmentStatus.BOUNCED, detail);
    }

    public void markUnsubscribed() {
        stopIfRunning(EnrollmentStatus.UNSUBSCRIBED, "lead unsubscribed");
    }

    public void markFailed(String detail) {
        stopIfRunning(EnrollmentStatus.FAILED, detail);
    }

    private void stopIfRunning(EnrollmentStatus terminal, String reason) {
        if (status.isTerminal()) {
            return;
        }
        stop(terminal, reason);
    }

    private void stop(EnrollmentStatus terminal, String reason) {
        this.status = terminal;
        this.endedAt = Instant.now();
        this.endReason = reason;
        this.nextActionAt = null;
    }

    private void requireActive() {
        requireNotTerminal();
        if (status != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("Enrollment is not active: " + getId());
        }
    }

    private void requireNotTerminal() {
        if (status.isTerminal()) {
            throw new IllegalStateException(
                    "Enrollment " + getId() + " is terminal (" + status + ") and cannot be modified");
        }
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public Lead getLead() {
        return lead;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public int getCurrentStepOrder() {
        return currentStepOrder;
    }

    public Instant getNextActionAt() {
        return nextActionAt;
    }

    public String getEndReason() {
        return endReason;
    }
}