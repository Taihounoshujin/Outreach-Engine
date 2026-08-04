package com.rushil.outreach.campaign.dto;

import com.rushil.outreach.campaign.CampaignStatus;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        String name,
        CampaignStatus status,
        String fromName,
        String fromEmail,
        int dailyCap,
        String timeZone,
        Instant createdAt) {
}