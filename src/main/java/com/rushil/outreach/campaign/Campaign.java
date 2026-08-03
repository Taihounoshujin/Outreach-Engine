package com.rushil.outreach.campaign;

import com.rushil.outreach.common.BaseEntity;
import com.rushil.outreach.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "campaign")
public class Campaign extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "from_name", nullable = false, length = 120)
    private String fromName;

    @Column(name = "from_email", nullable = false, length = 320)
    private String fromEmail;

    /**
     * Hard ceiling on sends per day for this campaign. Not yet enforced by the
     * scheduler — see the gap list in SETUP.md. Real mailboxes should stay well
     * under ~50/day; the default here is deliberately conservative.
     */
    @Column(name = "daily_cap", nullable = false)
    private int dailyCap = 40;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone = "UTC";

    protected Campaign() {
    }

    public Campaign(User owner, String name, String fromName, String fromEmail) {
        this.owner = owner;
        this.name = name;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
    }

    public void start() {
        if (status == CampaignStatus.ARCHIVED) {
            throw new IllegalStateException("Archived campaign cannot be started: " + getId());
        }
        this.status = CampaignStatus.RUNNING;
    }

    public void pause() {
        if (status != CampaignStatus.RUNNING) {
            throw new IllegalStateException("Only a running campaign can be paused: " + getId());
        }
        this.status = CampaignStatus.PAUSED;
    }

    public void archive() {
        this.status = CampaignStatus.ARCHIVED;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public int getDailyCap() {
        return dailyCap;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public void setDailyCap(int dailyCap) {
        this.dailyCap = dailyCap;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}