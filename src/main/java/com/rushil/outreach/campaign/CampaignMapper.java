package com.rushil.outreach.campaign;

import com.rushil.outreach.campaign.dto.CampaignResponse;
import com.rushil.outreach.campaign.dto.SequenceStepResponse;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {

    public CampaignResponse toResponse(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getStatus(),
                campaign.getFromName(),
                campaign.getFromEmail(),
                campaign.getDailyCap(),
                campaign.getTimeZone(),
                campaign.getCreatedAt());
    }

    public SequenceStepResponse toResponse(SequenceStep step) {
        return new SequenceStepResponse(
                step.getId(),
                step.getStepOrder(),
                step.getDelayDays(),
                step.getSubjectTemplate(),
                step.getBodyTemplate());
    }
}