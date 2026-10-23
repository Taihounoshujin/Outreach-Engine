package com.rushil.outreach.campaign;

import com.rushil.outreach.common.CreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sequence_step")
public class SequenceStep extends CreatedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false, updatable = false)
    private Campaign campaign;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "delay_days", nullable = false)
    private int delayDays;

    @Column(name = "subject_template", nullable = false, columnDefinition = "text")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "text")
    private String bodyTemplate;

    protected SequenceStep() {
    }

    public SequenceStep(Campaign campaign, int stepOrder, int delayDays,
                        String subjectTemplate, String bodyTemplate) {
        this.campaign = campaign;
        this.stepOrder = stepOrder;
        this.delayDays = delayDays;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public int getDelayDays() {
        return delayDays;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }
}