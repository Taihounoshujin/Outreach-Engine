package com.rushil.outreach.lead.dto;

import com.rushil.outreach.lead.LeadSource;

import java.time.Instant;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String email,
        String contactName,
        String companyName,
        String website,
        String industry,
        String city,
        String region,
        LeadSource source,
        Instant createdAt) {
}